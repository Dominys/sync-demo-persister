package com.sync.demo.syncdemo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class DownloadResponse {
    private String jobId;
    private String id;
    private Status status;
    private Object data;
}
