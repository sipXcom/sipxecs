package org.sipfoundry.sipxconfig.site.didpool;

import java.util.List;

import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.sipfoundry.commons.diddb.ActiveNextDid;
import org.sipfoundry.commons.diddb.Did;
import org.sipfoundry.commons.diddb.DidPool;
import org.sipfoundry.commons.diddb.DidPoolService;
import org.sipfoundry.commons.diddb.DidService;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.site.common.ListPanel;

public abstract class ListStartEndPanel extends ListPanel {

    @InjectObject(value = "spring:didPoolService")
    public abstract DidPoolService getDidPoolService();

    @InjectObject(value = "spring:didService")
    public abstract DidService getDidService();

    @Parameter(required=true)
    public abstract String getNextValue();

    public abstract void setNextValue(String nextValue);

    @Parameter(required = true)
    public abstract String getUserNextValue();

    public abstract void setUserNextValue(String userNextValue);


    @Override
    public void setSize(int size) {
        List source = getSource();
        while (size < source.size()) {
            source.remove(source.size() - 1);
        }
        while (size > source.size()) {
            source.add(new DidPool("", "", ""));
        }
    }

    @Override
    protected void afterRewind(IRequestCycle cycle) {
        List source = getSource();
        int removeIndex = getRemoveIndex();
        if (removeIndex >= 0) {
            getDidPoolService().removeDidPool(((DidPool)source.get(removeIndex)));
            source.remove(removeIndex);
            TapestryUtils.getValidator(this).clearErrors();
        } else if (TapestryUtils.isValid(cycle, this) && getAdd()) {
            source.add(new DidPool("","",""));
        }
        if (getNextValue() != null) {
            setUserNextValue(getNextValue());
        }
        Did activeDid = getDidService().getActiveNextDid();
        setNextValue(activeDid == null ? null : activeDid.getValue());
    }

    public void saveNext(String poolId, String value) {
        ActiveNextDid activeNextDid = (ActiveNextDid)getDidService().getActiveNextDid();
        if (activeNextDid == null) {
            activeNextDid = new ActiveNextDid(null, null, null, null);
        }
        activeNextDid.setPoolId(poolId);
        activeNextDid.setValue(value);
        getDidService().saveDid(activeNextDid);
    }
    
    public void commit() {
        for (Object obj : getSource()) {
            DidPool pool = (DidPool)obj;
            pool.setNext(getDidPoolService().findNext(pool).toString());
            getDidPoolService().saveDidPool(pool);
        }        
    }    
}
