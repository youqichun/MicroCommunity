package com.java110.api.listener.fee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.fee.IFeeBMO;
import com.java110.api.listener.AbstractServiceApiDataFlowListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.dto.fee.FeeAttrDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.order.BusinessDto;
import com.java110.dto.repair.RepairDto;
import com.java110.entity.center.AppService;
import com.java110.entity.order.Orders;
import com.java110.intf.fee.IFeeAttrInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.order.IOrderInnerServiceSMO;
import com.java110.po.owner.RepairPoolPo;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * @ClassName PayFeeListener
 * @Description TODO 交费通知侦听
 * @Author wuxw
 * @Date 2019/6/3 13:46
 * @Version 1.0
 * add by wuxw 2019/6/3
 **/
@Java110Listener("PayFeeConfirmListener")
public class PayFeeConfirmListener extends AbstractServiceApiDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(PayFeeConfirmListener.class);

    @Autowired
    private IFeeBMO feeBMOImpl;

    @Autowired
    private IFeeAttrInnerServiceSMO feeAttrInnerServiceSMOImpl;

    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IOrderInnerServiceSMO orderInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_PAY_CONFIRM_PRE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public void soService(ServiceDataFlowEvent event) {

        logger.debug("ServiceDataFlowEvent : {}", event);

        DataFlowContext dataFlowContext = event.getDataFlowContext();
        AppService service = event.getAppService();

        String paramIn = dataFlowContext.getReqData();

        //校验数据
        validate(paramIn);
        JSONObject paramObj = JSONObject.parseObject(paramIn);

        HttpHeaders header = new HttpHeaders();
        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();
        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.O_ID, paramObj.getString("oId"));
        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.ORDER_PROCESS, Orders.ORDER_PROCESS_ORDER_CONFIRM_SUBMIT);
        ResponseEntity<String> responseEntity = feeBMOImpl.callService(dataFlowContext, service.getServiceCode(), businesses);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            dataFlowContext.setResponseEntity(responseEntity);
            return;
        }

        //根据oId 查询 bId;
        BusinessDto businessDto = new BusinessDto();
        businessDto.setoId(paramObj.getString("oId"));
        businessDto.setBusinessTypeCd("600100040001");
        List<BusinessDto> businessDtos = orderInnerServiceSMOImpl.querySameOrderBusiness(businessDto);

        if (businessDtos == null || businessDtos.size() < 1) {
            dataFlowContext.setResponseEntity(responseEntity);
            return;
        }

        FeeDto feeDto = new FeeDto();
        feeDto.setbId(businessDtos.get(0).getbId());

        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryBusinessFees(feeDto);

        if (feeDtos == null || feeDtos.size() < 1) {
            dataFlowContext.setResponseEntity(responseEntity);
            return;
        }


        businesses = new JSONArray();
        //判断是否有派单属性ID
        FeeAttrDto feeAttrDto = new FeeAttrDto();
        feeAttrDto.setCommunityId(feeDtos.get(0).getCommunityId());
        feeAttrDto.setFeeId(feeDtos.get(0).getFeeId());
        feeAttrDto.setSpecCd(FeeAttrDto.SPEC_CD_REPAIR);
        List<FeeAttrDto> feeAttrDtos = feeAttrInnerServiceSMOImpl.queryFeeAttrs(feeAttrDto);
        //修改 派单状态
        if (feeAttrDtos != null && feeAttrDtos.size() > 0) {
            JSONObject business = JSONObject.parseObject("{\"datas\":{}}");
            business.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_UPDATE_REPAIR);
            business.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ + 1);
            business.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
            RepairPoolPo repairPoolPo = new RepairPoolPo();
            repairPoolPo.setRepairId(feeAttrDtos.get(0).getValue());
            repairPoolPo.setCommunityId(paramObj.getString("communityId"));
            repairPoolPo.setState(RepairDto.STATE_APPRAISE);
            business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put(RepairPoolPo.class.getSimpleName(), BeanConvertUtil.beanCovertMap(repairPoolPo));
            businesses.add(business);
            dataFlowContext.getRequestCurrentHeaders().remove(CommonConstant.ORDER_PROCESS);
            dataFlowContext.getRequestCurrentHeaders().remove(CommonConstant.O_ID);
            feeBMOImpl.callService(dataFlowContext, service.getServiceCode(), businesses);
        }

        dataFlowContext.setResponseEntity(responseEntity);


    }

    /**
     * 刷入order信息
     *
     * @param orders  订单信息
     * @param headers 头部信息
     */
    protected void freshOrderProtocol(JSONObject orders, Map<String, String> headers) {
        feeBMOImpl.freshOrderProtocol(orders, headers);
        orders.put("orderProcess", Orders.ORDER_PROCESS_ORDER_CONFIRM_SUBMIT);
        orders.put("oId", headers.get("oId"));
    }

    /**
     * 数据校验
     *
     * @param paramIn "communityId": "7020181217000001",
     *                "memberId": "3456789",
     *                "memberTypeCd": "390001200001"
     */
    private void validate(String paramIn) {
        Assert.jsonObjectHaveKey(paramIn, "oId", "请求报文中未包含订单信息");
        JSONObject paramInObj = JSONObject.parseObject(paramIn);
        Assert.hasLength(paramInObj.getString("oId"), "订单信息不能为空");

    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

}
