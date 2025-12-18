package com.common.jane_ai.mybatisplus;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Date;

//MyBatis Plus 的自动填充功能
@Slf4j//自动再更新时补充更新时间与创建时间
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {
// - metaObject: 元对象处理器，用于获取和设置实体类属性值
// - fieldName: 需要填充的字段名称
// - fieldType: 字段类型，用于类型安全检查
// - fieldVal: 要填充的字段值，必须是fieldType的子类型
    @Override
    public void insertFill(MetaObject metaObject) {
        //logger默认为当前全限定类名
        log.info("MybatisPlus Auto insert fill ....");
        this.strictUpdateFill(metaObject, "updateTime",LocalDateTime.class, DateUtil.date().toLocalDateTime());
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, DateUtil.date().toLocalDateTime());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("MybatisPlus Auto update fill ....");
        this.strictUpdateFill(metaObject, "updateTime",LocalDateTime.class, DateUtil.date().toLocalDateTime());
    }
}
