package com.java110.goods.bmo.storeOrderAddress.impl;

import com.java110.core.annotation.Java110Transactional;
import com.java110.goods.bmo.storeOrderAddress.IDeleteStoreOrderAddressBMO;
import com.java110.intf.goods.IStoreOrderAddressInnerServiceSMO;
import com.java110.po.storeOrderAddress.StoreOrderAddressPo;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("deleteStoreOrderAddressBMOImpl")
public class DeleteStoreOrderAddressBMOImpl implements IDeleteStoreOrderAddressBMO {

    @Autowired
    private IStoreOrderAddressInnerServiceSMO storeOrderAddressInnerServiceSMOImpl;

    /**
     * @param storeOrderAddressPo 数据
     * @return 订单服务能够接受的报文
     */
    @Java110Transactional
    public ResponseEntity<String> delete(StoreOrderAddressPo storeOrderAddressPo) {

        int flag = storeOrderAddressInnerServiceSMOImpl.deleteStoreOrderAddress(storeOrderAddressPo);

        if (flag > 0) {
            return ResultVo.createResponseEntity(ResultVo.CODE_OK, "保存成功");
        }

        return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "保存失败");
    }

}
