package com.elwg.ai3dbackend.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ChatRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long sessionId;
    private String message;
}