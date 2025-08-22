package io.github.xzy.novel.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.xzy.novel.core.annotation.Desensitization;
import io.github.xzy.novel.core.common.constant.DesensitizationTypeEnum;
import io.github.xzy.novel.core.json.serializer.DesensitizationSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户信息 响应DTO
 *
 * @author xiongxiaoyang
 * @date 2022/5/22
 */
@Data
@Builder
public class UserInfoRespDto {

    /**
     * 昵称
     * */
    @Schema(description = "昵称")
    @JsonSerialize(using = DesensitizationSerialize.class)
    @Desensitization(type = DesensitizationTypeEnum.MOBILE_PHONE)
    private String nickName;

    /**
     * 用户头像
     * */
    @Schema(description = "用户头像")
    private String userPhoto;

    /**
     * 用户性别
     * */
    @Schema(description = "用户性别")
    private Integer userSex;
}
