package com.tuya.iot.suite.web.controller;

import com.tuya.iot.suite.ability.user.model.MobileCountries;
import com.tuya.iot.suite.core.constant.Response;
import com.tuya.iot.suite.core.exception.ServiceLogicException;
import com.tuya.iot.suite.core.util.LibPhoneNumberUtil;
import com.tuya.iot.suite.core.util.MixUtil;
import com.tuya.iot.suite.core.util.Todo;
import com.tuya.iot.suite.service.model.PageDataVO;
import com.tuya.iot.suite.service.user.UserService;
import com.tuya.iot.suite.service.user.model.ResetPasswordBo;
import com.tuya.iot.suite.web.i18n.I18nMessage;
import com.tuya.iot.suite.web.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import java.util.List;

import static com.tuya.iot.suite.core.constant.ErrorCode.*;


/**
 * @author bade,benguan
 */
@RestController
@Slf4j
@Api(value = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private HttpSession session;
    @Autowired
    private I18nMessage i18nMessage;

    /**
     * @return
     */
    @ApiOperation(value = "修改密码")
    @SneakyThrows
    @PutMapping(value = "/user/password")
    public Response modifyLoginPassword(@RequestBody UserPasswordModifyReq req) {
        Boolean modifyLoginPassword = userService.modifyLoginPassword(req.getUid(), req.getOldPassword(), req.getNewPassword());
        return modifyLoginPassword ? Response.buildSuccess(true) :
                Response.buildFailure(USER_NOT_EXIST.getCode(), i18nMessage.getMessage(USER_NOT_EXIST.getCode(), USER_NOT_EXIST.getMsg()));
    }

/*    *//**
     * @see MyController#restPasswordCaptcha
     * *//*
    @ApiOperation(value = "获取密码重置验证码")
    @PostMapping(value = "/user/password/reset/captcha")
    public Response<Boolean> restPasswordCaptcha(@RequestBody ResetPasswordReq req) {
        resetPasswordCheck(req);
        CaptchaPushBo captchaPushBo = new CaptchaPushBo();
        captchaPushBo.setLanguage(req.getLanguage());
        captchaPushBo.setCountryCode(req.getCountryCode());
        captchaPushBo.setPhone(req.getPhone());
        captchaPushBo.setMail(req.getMail());
        return Response.buildSuccess(userService.sendRestPasswordCaptcha(captchaPushBo));
    }*/

    @ApiOperation(value = "用户密码重置")
    @PostMapping(value = "/user/password/reset")
    public Response<Boolean> resetPassword(@RequestBody @Valid ResetPasswordReq req) {
        // 参数校验
        resetPasswordCheck(req);
        if (StringUtils.isEmpty(req.getCode()) || StringUtils.isEmpty(req.getNewPassword())) {
            return Response.buildFailure(PARAM_ERROR);
        }
        ResetPasswordBo resetPasswordBo = new ResetPasswordBo();
        resetPasswordBo.setLanguage(req.getLanguage());
        resetPasswordBo.setMail(req.getMail());
        resetPasswordBo.setCountryCode(req.getCountryCode());
        resetPasswordBo.setPhone(req.getPhone());
        resetPasswordBo.setPassword(req.getNewPassword());
        resetPasswordBo.setCode(req.getCode());
        return Response.buildSuccess(userService.resetPassword(resetPasswordBo));
    }

    @ApiOperation(value = "获取手机号国际区号")
    @GetMapping(value = "/mobile/countries")
    public Response<MobileCountries> selectMobileCountries() {
        return new Response<>(userService.selectMobileCountries());
    }

    /**
     * 重置密码参数校验
     *
     * @param req
     */
    private void resetPasswordCheck(ResetPasswordReq req) {
        if (StringUtils.isEmpty(req.getMail())
                && StringUtils.isEmpty(req.getCountryCode())
                && StringUtils.isEmpty(req.getPhone())) {
            throw new ServiceLogicException(PARAM_ERROR);
        }
        // 验证手机格式
        if (!StringUtils.isEmpty(req.getCountryCode())
                && !StringUtils.isEmpty(req.getPhone())) {
            if (!LibPhoneNumberUtil.doValid(req.getPhone(), req.getCountryCode())) {
                log.info("telephone format error! =>{}{}", req.getCountryCode(), req.getPhone());
                throw new ServiceLogicException(TELEPHONE_FORMAT_ERROR);
            }
        }
        // 验证邮箱格式
        if (!StringUtils.isEmpty(req.getMail()) && !MixUtil.mailFormatValidate(req.getMail())) {
            log.info("mail format error! => [{}]", req.getMail());
            throw new ServiceLogicException(PARAM_ERROR);
        }
    }

    @ApiOperation("创建用户")
    @PutMapping("/users")
    public Response<Boolean> createUser(@RequestBody UserCreateReq userCreateReq) {
        return Todo.todo("新增用户，重置密码");
    }

    @ApiOperation("修改用户名称")
    @PutMapping("/users/{uid}/name")
    public Response<Boolean> updateUserName(@ApiParam(value = "用户id") @PathVariable String uid,
                                            @RequestBody UserNameUpdateReq req) {
        return Todo.todo();
    }

    /**
     * 批量操作，统一按"resources-batch"方式定义路径。（谁有好的建议可以提出来）
     */
    @ApiOperation("批量删除用户")
    @DeleteMapping("/batch-users")
    public Response<Boolean> batchDeleteUser(@ApiParam(value = "uid列表，逗号分隔", required = true)
                                             @RequestParam String uidList) {
        return Todo.todo();
    }

    @ApiOperation("用户列表")
    @GetMapping("/users")
    public Response<PageDataVO<UserVO>> listUsers(@ApiParam(value = "搜索关键字") String searchKey,
                                                  @ApiParam(value = "角色编码") String roleCode) {
        return Todo.todo();
    }

    @ApiOperation("用户权限列表")
    @GetMapping("/users/{uid}/permissions")
    public Response<List<UserPermissionVO>> listUserPermissions(
            @ApiParam(value = "用户id") @PathVariable String uid) {
        return Todo.todo();
    }

}
