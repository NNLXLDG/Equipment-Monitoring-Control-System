package com.emcs.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端 SPA 路由回退：非 API、非静态资源的请求转发到 index.html。
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/", "/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
