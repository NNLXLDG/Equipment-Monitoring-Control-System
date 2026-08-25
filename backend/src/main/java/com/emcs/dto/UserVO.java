package com.emcs.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<Long> roleIds = new ArrayList<>();
    private List<String> roleNames = new ArrayList<>();
}
