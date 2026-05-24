package com.example.smart_study.entity;

import java.util.Date;
import lombok.Data;

@Data  // <--- 有了它，下面就不需要手写 getter 和 setter 了！
public class Note {
    private Integer id;
    private Integer userId;
    private String fileName;
    private String filePath;
    private Date uploadTime;
}
