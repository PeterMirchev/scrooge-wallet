package com.scrooge.web;

import com.scrooge.service.AuditLogService;
import com.scrooge.service.UserService;
import org.springframework.stereotype.Controller;


@Controller
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final UserService userService;

    public AuditLogController(AuditLogService auditLogService, UserService userService) {
        this.auditLogService = auditLogService;
        this.userService = userService;
    }

}
