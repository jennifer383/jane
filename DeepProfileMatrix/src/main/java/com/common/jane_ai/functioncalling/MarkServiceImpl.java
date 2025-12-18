package com.common.jane_ai.functioncalling;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.jane_ai.Obj.entity.PDFentity;
import com.common.jane_ai.mapper.PDFMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;
import java.time.LocalDateTime;

@Slf4j
@Service
public class MarkServiceImpl {

    @Autowired
    private PDFMapper pdfMapper;

    /**
     * 后期并发量高可以用 redis 缓存
     * @param score
     * @param userName
     * @param password
     * @return
     */
    @Transactional
    public String mark(Double score,String userName,String password) {
        log.info("上传用户分数+"+score+":"+Thread.currentThread().getName());
        LambdaQueryWrapper<PDFentity> wrapper = Wrappers.lambdaQuery(PDFentity.class)
                .eq(PDFentity::getUserName, userName)
                .eq(PDFentity::getStatus, true);
        PDFentity pdfentity = pdfMapper.selectOne(wrapper);

        if(pdfentity!=null){//  有数据

            if(!pdfentity.getPassword().equals(password)){
                return "密码或用户名错误";
            }

            LambdaUpdateWrapper<PDFentity> updateWrapper = Wrappers.lambdaUpdate(PDFentity.class)
                    .set(PDFentity::getPdfScore, score)
                    .setIncrBy(PDFentity::getPdfUpdateCount, 1L)
                    .set(PDFentity::getUpdateTime, LocalDateTime.now())
                    .eq(PDFentity::getUserName, userName);
            try {
                pdfMapper.update(null,updateWrapper);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("更新用户分数失败");
            }
        }else {
            pdfentity = new PDFentity();
            pdfentity.setUserId(1L);
            pdfentity.setUserName(userName);
            pdfentity.setPdfScore(score);
            pdfentity.setStatus(true);
            pdfentity.setPdfUpdateCount(1);
            pdfentity.setPassword(password);
            try {
                pdfMapper.insert(pdfentity);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("上传用户分数失败");
            }
        }

        return "上传成功";
    }
}
