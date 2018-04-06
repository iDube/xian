package info.xiancloud.core.message;

import info.xiancloud.core.*;
import info.xiancloud.core.distribution.GroupJudge;
import info.xiancloud.core.message.sender.IAsyncSender;
import info.xiancloud.core.message.sender.SenderFactory;
import info.xiancloud.core.message.sender.virtureunit.DefaultVirtualUnitConverter;
import info.xiancloud.core.message.sender.virtureunit.IVirtualUnitConverter;
import info.xiancloud.core.message.sender.virtureunit.VirtualDaoUnitConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for calling unit locally or remotely.
 *
 * @author happyyangyuan
 * @deprecated deprecated because this callback style may produce callback hell.
 */
class Xian /*extends SyncXian*/ {

    /**
     * @param destinationUnitBean the destination unit description bean. group name and unit name required.
     * @param map                 parameters
     * @param handler             call back handler
     */
    public static void call(Unit destinationUnitBean, Map<String, Object> map, Handler<UnitResponse> handler) {
        call(destinationUnitBean.getGroup().getName(), destinationUnitBean.getName(), map, handler);
    }

    /**
     * @param destinationUnitBean the destination unit description bean. group name and unit name required.
     * @param bean                parameter bean
     * @param handler             call back handler
     */
    public static void call(Unit destinationUnitBean, Bean bean, Handler<UnitResponse> handler) {
        call(destinationUnitBean.getGroup().getName(), destinationUnitBean.getName(), bean, handler);
    }

    public static void call(String group, String unit, Map<String, Object> map, Handler<UnitResponse> handler) {
        UnitRequest request = UnitRequest.create(group, unit).setArgMap(map);
        call(request, handler);
    }

    public static void call(String group, String unit, Bean bean, Handler<UnitResponse> handler) {
        call(group, unit, bean.toMap(), handler);
    }

    public static void call(Class<? extends Unit> unitClass, Map<String, Object> map, Handler<UnitResponse> handler) {
        call(LocalUnitsManager.getGroupByUnitClass(unitClass).getName(),
                LocalUnitsManager.getUnitByUnitClass(unitClass).getName(),
                map,
                handler);
    }

    public static void call(Class<? extends Unit> unitClass, Bean bean, Handler<UnitResponse> handler) {
        call(unitClass, bean.toMap(), handler);
    }

    /**
     * Tell the DAO layer to query data from the read-only database.
     */
    public static void readonly(String daoGroup, String daoUnit, Map<String, Object> map, Handler<UnitResponse> handler) {
        UnitRequest request = UnitRequest.create(daoGroup, daoUnit).setArgMap(map);
        request.getContext().setReadyOnly(true);
        call(request, handler);
    }

    /**
     * Tell the DAO layer to query data from the read-only database.
     */
    public static void readonly(String daoGroup, String daoUnit, Bean bean, Handler<UnitResponse> handler) {
        readonly(daoGroup, daoUnit, bean.toMap(), handler);
    }

    /**
     * call the specified unit without parameters
     */
    public static void call(String group, String unit, Handler<UnitResponse> handler) {
        Xian.call(group, unit, new HashMap<>(), handler);
    }

    /**
     * call the specified unit without parameters
     */
    public static void call(Class<? extends Unit> unitClass, Handler<UnitResponse> handler) {
        call(unitClass, new HashMap<>(), handler);
    }

    public static void call(UnitRequest request, Handler<UnitResponse> handler) {
        String group = request.getContext().getGroup(),
                unit = request.getContext().getUnit();
        String concretedUnitName = getConverter(group).getConcreteUnit(group, unit, request.getArgMap());
        request.getContext().setUnit(concretedUnitName).setVirtualUnit(unit);
        IAsyncSender sender = SenderFactory.getSender(request, new NotifyHandler() {
            @Override
            protected void handle(UnitResponse unitResponse) {
                handler.handle(unitResponse);
            }
        });
        sender.send();
    }

    static IVirtualUnitConverter getConverter(String group) {
        if (GroupJudge.defined(group) && GroupJudge.isDao(group)) {
            return VirtualDaoUnitConverter.singleton;
        }
        return DefaultVirtualUnitConverter.singleton;
    }

}
