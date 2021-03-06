package com.java110.goods.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.DAOException;
import com.java110.utils.util.DateUtil;
import com.java110.core.base.dao.BaseServiceDao;
import com.java110.goods.dao.IProductSpecServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 产品规格服务 与数据库交互
 * Created by wuxw on 2017/4/5.
 */
@Service("productSpecServiceDaoImpl")
//@Transactional
public class ProductSpecServiceDaoImpl extends BaseServiceDao implements IProductSpecServiceDao {

    private static Logger logger = LoggerFactory.getLogger(ProductSpecServiceDaoImpl.class);





    /**
     * 保存产品规格信息 到 instance
     * @param info   bId 信息
     * @throws DAOException DAO异常
     */
    @Override
    public void saveProductSpecInfo(Map info) throws DAOException {
        logger.debug("保存产品规格信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.insert("productSpecServiceDaoImpl.saveProductSpecInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"保存产品规格信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }


    /**
     * 查询产品规格信息（instance）
     * @param info bId 信息
     * @return List<Map>
     * @throws DAOException DAO异常
     */
    @Override
    public List<Map> getProductSpecInfo(Map info) throws DAOException {
        logger.debug("查询产品规格信息 入参 info : {}",info);

        List<Map> businessProductSpecInfos = sqlSessionTemplate.selectList("productSpecServiceDaoImpl.getProductSpecInfo",info);

        return businessProductSpecInfos;
    }


    /**
     * 修改产品规格信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    @Override
    public void updateProductSpecInfo(Map info) throws DAOException {
        logger.debug("修改产品规格信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.update("productSpecServiceDaoImpl.updateProductSpecInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"修改产品规格信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }

     /**
     * 查询产品规格数量
     * @param info 产品规格信息
     * @return 产品规格数量
     */
    @Override
    public int queryProductSpecsCount(Map info) {
        logger.debug("查询产品规格数据 入参 info : {}",info);

        List<Map> businessProductSpecInfos = sqlSessionTemplate.selectList("productSpecServiceDaoImpl.queryProductSpecsCount", info);
        if (businessProductSpecInfos.size() < 1) {
            return 0;
        }

        return Integer.parseInt(businessProductSpecInfos.get(0).get("count").toString());
    }


}
