package com.nkduy.restaurant_application.mapper;

import com.nkduy.restaurant_application.branch.dto.response.BranchInfoResponse;
import com.nkduy.restaurant_application.branch.entity.Branch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {
    public BranchInfoResponse toBranchInfoResponse(Branch branch){
        return new BranchInfoResponse(
                branch.getId(),
                branch.getName(),
                branch.getAddress(),
                branch.getLongitude(),
                branch.getLatitude(),
                branch.getStatus().toString()
        );
    }
}
