package com.tuya.iot.suite.web.model.request.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @author: benguan.zhou@tuya.com
 * @date: 2021/05/28
 */
@Getter
@Setter
@ToString
public class BatchUserGrantRoleReq implements Serializable {


    @ApiModelProperty(value="权限编码",required = true)
    String roleCode;

    @ApiModelProperty(value="用户id列表",required = true)
    List<String> userIds;

}