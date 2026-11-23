package com.nkduy.restaurant_application;

//package com.nkduy.restaurant_application.branch.service;

import com.nkduy.restaurant_application.branch.dto.request.CreateBranchRequest;
import com.nkduy.restaurant_application.branch.entity.Branch;
import com.nkduy.restaurant_application.branch.entity.BranchStatus;
import com.nkduy.restaurant_application.branch.repository.BranchRepository;
import com.nkduy.restaurant_application.branch.service.BranchService;
import com.nkduy.restaurant_application.ingredient.entity.BranchIngredient;
import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import com.nkduy.restaurant_application.ingredient.entity.StockStatus;
import com.nkduy.restaurant_application.ingredient.repository.BranchIngredientRepository;
import com.nkduy.restaurant_application.ingredient.repository.IngredientRepository;
import com.nkduy.restaurant_application.mapper.BranchMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock BranchRepository branchRepository;
    @Mock BranchMapper branchMapper;
    @Mock IngredientRepository ingredientRepository;
    @Mock BranchIngredientRepository branchIngredientRepository;

    @InjectMocks
    BranchService branchService;

    @Captor ArgumentCaptor<List<BranchIngredient>> rowsCaptor;
    @Captor ArgumentCaptor<Branch> branchCaptor;

    private Branch savedBranch;
    private CreateBranchRequest request;

    @BeforeEach
    void setUp() {
        savedBranch = Branch.builder().id("branch-1").name("Branch 1").status(BranchStatus.ACTIVE).build();
        request = new CreateBranchRequest("Branch 1", "1 Main St",
                BigDecimal.valueOf(106.7), BigDecimal.valueOf(10.77));

        // lenient: the getBranchById test below doesn't call save
        lenient().when(branchRepository.save(any(Branch.class))).thenReturn(savedBranch);
    }

    @Test
    void createBranch_createsOneEmptyRowPerIngredient() {
        when(ingredientRepository.findAll())
                .thenReturn(List.of(ingredient("i1"), ingredient("i2"), ingredient("i3")));
        when(branchIngredientRepository.findIngredientIdsByBranchId("branch-1")).thenReturn(Set.of());

        branchService.createBranch(request);

        verify(branchIngredientRepository).saveAll(rowsCaptor.capture());
        List<BranchIngredient> rows = rowsCaptor.getValue();

        assertEquals(3, rows.size());
        for (BranchIngredient row : rows) {
            assertSame(savedBranch, row.getBranch());
            assertEquals(0, BigDecimal.ZERO.compareTo(row.getQuantity()));
            assertEquals(StockStatus.OUT_OF_STOCK, row.getStatus());
        }
        assertEquals(Set.of("i1", "i2", "i3"),
                rows.stream().map(r -> r.getIngredient().getId()).collect(Collectors.toSet()));
    }

    @Test
    void createBranch_whenThereAreNoIngredients_stillCreatesTheBranch() {
        when(ingredientRepository.findAll()).thenReturn(List.of());
        when(branchIngredientRepository.findIngredientIdsByBranchId("branch-1")).thenReturn(Set.of());

        assertNotNull(branchService.createBranch(request));

        verify(branchIngredientRepository).saveAll(rowsCaptor.capture());
        assertTrue(rowsCaptor.getValue().isEmpty());
    }

    @Test
    void createBranch_skipsIngredientsThatAreAlreadyLinked() {
        // simulates running the linking a second time
        when(ingredientRepository.findAll())
                .thenReturn(List.of(ingredient("i1"), ingredient("i2"), ingredient("i3")));
        when(branchIngredientRepository.findIngredientIdsByBranchId("branch-1")).thenReturn(Set.of("i2"));

        branchService.createBranch(request);

        verify(branchIngredientRepository).saveAll(rowsCaptor.capture());
        assertEquals(Set.of("i1", "i3"),
                rowsCaptor.getValue().stream().map(r -> r.getIngredient().getId()).collect(Collectors.toSet()));
    }

    @Test
    void createBranch_savesAnActiveBranchBeforeLinkingIngredients() {
        when(ingredientRepository.findAll()).thenReturn(List.of(ingredient("i1")));
        when(branchIngredientRepository.findIngredientIdsByBranchId("branch-1")).thenReturn(Set.of());

        branchService.createBranch(request);

        InOrder order = inOrder(branchRepository, branchIngredientRepository);
        order.verify(branchRepository).save(branchCaptor.capture());
        order.verify(branchIngredientRepository).saveAll(any());

        assertEquals(BranchStatus.ACTIVE, branchCaptor.getValue().getStatus());
        assertEquals("Branch 1", branchCaptor.getValue().getName());
    }

    @Test
    void getBranchById_whenMissing_throws404() {
        when(branchRepository.findById("nope")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> branchService.getBranchById("nope"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    private static Ingredient ingredient(String id) {
        return Ingredient.builder().id(id).build();
    }
}