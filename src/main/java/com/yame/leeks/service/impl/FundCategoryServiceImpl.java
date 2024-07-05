package com.yame.leeks.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yame.leeks.entity.FundCategory;
import com.yame.leeks.mapper.FundCategoryMapper;
import com.yame.leeks.service.FundCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基金分类接口实现
 * @author yangmeng
 */
@Slf4j
@Service
@Transactional
public class FundCategoryServiceImpl extends ServiceImpl<FundCategoryMapper, FundCategory> implements FundCategoryService {

    @Autowired
    private FundCategoryMapper fundCategoryMapper;


}
