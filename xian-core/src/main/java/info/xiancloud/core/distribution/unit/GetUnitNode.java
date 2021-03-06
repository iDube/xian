package info.xiancloud.core.distribution.unit;

import info.xiancloud.core.*;
import info.xiancloud.core.distribution.exception.UnitOfflineException;
import info.xiancloud.core.distribution.exception.UnitUndefinedException;
import info.xiancloud.core.distribution.loadbalance.UnitRouter;
import info.xiancloud.core.message.UnitRequest;
import info.xiancloud.core.message.UnitResponse;

/**
 * find out which node the group/unit is in
 *
 * @author happyyangyuan
 */
public class GetUnitNode implements Unit {
    @Override
    public Group getGroup() {
        return SystemGroup.singleton;
    }

    @Override
    public String getName() {
        return "getUnitNode";
    }

    @Override
    public UnitMeta getMeta() {
        return UnitMeta.create().setDescription("find out which node the group/unit is in")
                .setDocApi(false)
                ;
    }

    @Override
    public Input getInput() {
        return new Input()
                .add("group", String.class, "group name", REQUIRED)
                .add("unit", String.class, "unit name", REQUIRED);
    }

    @Override
    public void execute(UnitRequest msg, Handler<UnitResponse> handler) {
        String group = msg.getString("group");
        String unit = msg.getString("unit");
        try {
            handler.handle(UnitResponse.createSuccess(UnitRouter.singleton.allInstances(Unit.fullName(group, unit))));
        } catch (UnitOfflineException | UnitUndefinedException e) {
            handler.handle(UnitResponse.createUnknownError(null, e.getLocalizedMessage()));
        }
    }
}
