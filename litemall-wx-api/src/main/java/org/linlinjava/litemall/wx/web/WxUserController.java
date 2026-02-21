package org.linlinjava.litemall.wx.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallUser;
import org.linlinjava.litemall.db.domain.LitemallUserLevel;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.service.LitemallUserLevelService;
import org.linlinjava.litemall.db.service.LitemallUserService;
import org.linlinjava.litemall.wx.annotation.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用户服务
 */
@RestController
@RequestMapping("/wx/user")
@Validated
public class WxUserController {
    private final Log logger = LogFactory.getLog(WxUserController.class);

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");

    @Autowired
    private LitemallOrderService orderService;
    @Autowired
    private LitemallUserService userService;
    @Autowired
    private LitemallUserLevelService userLevelService;

    /**
     * 用户个人页面数据
     * <p>
     * 目前是用户订单统计信息
     *
     * @param userId 用户ID
     * @return 用户个人页面数据
     */
    @GetMapping("index")
    public Object list(@LoginUser Integer userId) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("order", orderService.orderInfo(userId));
        LitemallUser user = userService.findById(userId);
        data.put("points", user == null ? 0 : user.getPoints());
        Integer experience = user == null ? 0 : (user.getExperience() == null ? 0 : user.getExperience());
        data.put("experience", experience);
        LitemallUserLevel level = userLevelService.findByExperience(experience);
        data.put("levelName", level == null ? null : level.getLevelName());
        return ResponseUtil.ok(data);
    }

    /**
     * 获取用户实名信息
     *
     * @param userId 用户ID
     * @return 实名信息
     */
    @GetMapping("idcard")
    public Object getIdCard(@LoginUser Integer userId) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        LitemallUser user = userService.findById(userId);
        if (user == null) {
            return ResponseUtil.badArgument();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("realName", user.getRealName());
        data.put("idCard", maskIdCard(user.getIdCard()));
        data.put("bound", user.getRealName() != null && !user.getRealName().isEmpty() 
                && user.getIdCard() != null && !user.getIdCard().isEmpty());
        return ResponseUtil.ok(data);
    }

    /**
     * 绑定身份证
     *
     * @param userId 用户ID
     * @param body   请求体 {realName: xxx, idCard: xxx}
     * @return 操作结果
     */
    @PostMapping("idcard")
    public Object bindIdCard(@LoginUser Integer userId, @RequestBody Map<String, String> body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        String realName = body.get("realName");
        String idCard = body.get("idCard");

        if (realName == null || realName.trim().isEmpty()) {
            return ResponseUtil.fail(401, "真实姓名不能为空");
        }

        if (idCard == null || idCard.trim().isEmpty()) {
            return ResponseUtil.fail(402, "身份证号不能为空");
        }

        idCard = idCard.toUpperCase().trim();
        realName = realName.trim();

        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return ResponseUtil.fail(403, "身份证号格式不正确");
        }

        LitemallUser user = userService.findById(userId);
        if (user == null) {
            return ResponseUtil.badArgument();
        }

        if (user.getRealName() != null && !user.getRealName().isEmpty()) {
            return ResponseUtil.fail(404, "已绑定身份证，如需修改请联系客服");
        }

        LitemallUser updateUser = new LitemallUser();
        updateUser.setId(userId);
        updateUser.setRealName(realName);
        updateUser.setIdCard(idCard);
        updateUser.setUpdateTime(LocalDateTime.now());
        userService.updateById(updateUser);

        Map<String, Object> data = new HashMap<>();
        data.put("realName", realName);
        data.put("idCard", maskIdCard(idCard));
        data.put("bound", true);
        return ResponseUtil.ok(data);
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

}
