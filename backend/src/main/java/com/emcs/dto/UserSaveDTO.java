package com.emcs.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserSaveDTO {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private String remark;
    private List<Long> roleIds;
}
