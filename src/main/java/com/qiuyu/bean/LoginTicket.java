package com.qiuyu.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Date;

/**
 * @author QiuYuSY
 * @create 2023-01-18 14:11
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginTicket implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer userId;
    private String ticket;
    private Integer status;
    private Date expired;

}
