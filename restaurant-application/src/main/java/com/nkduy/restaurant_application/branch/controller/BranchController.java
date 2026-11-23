package com.nkduy.restaurant_application.branch.controller;

import com.nkduy.restaurant_application.auth.dto.ApiResponse;
import com.nkduy.restaurant_application.branch.dto.request.CreateBranchRequest;
import com.nkduy.restaurant_application.branch.dto.request.UpdateBranchRequest;
import com.nkduy.restaurant_application.branch.dto.response.BranchInfoResponse;
import com.nkduy.restaurant_application.branch.dto.response.CreateBranchResponse;
import com.nkduy.restaurant_application.branch.entity.BranchStatus;
import com.nkduy.restaurant_application.branch.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.ASC;

@RestController
@RequestMapping("/api/v1/branch")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateBranchResponse> createBranch(@Valid @RequestBody CreateBranchRequest request){
        return ApiResponse.ok(branchService.createBranch(request));

    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<BranchInfoResponse>> getBranchList(
            @RequestParam(required = false) BranchStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "name",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ){
        return ApiResponse.ok(branchService.getAllBranchByStatus(status, pageable));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<BranchInfoResponse> updateBranch(@PathVariable String id, @Valid @RequestBody UpdateBranchRequest request){
        return ApiResponse.ok(branchService.updateBranch(id, request));
    }






}
