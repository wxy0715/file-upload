package com.wxy.file.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangxingyu
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value = "id/id集合传参model")
public class IdCmd {
    public interface SelectById{};
    public interface DeleteById{};

    @NotNull(message = "id传参不能为空",groups = {SelectById.class})
    @ApiModelProperty(value = "id传参")
    private Long id;

    @NotEmpty(message = "id集合传参不能为空",groups = {DeleteById.class})
    @ApiModelProperty(value = "id集合传参")
    private List<Long> idList = new ArrayList<Long>();
}
