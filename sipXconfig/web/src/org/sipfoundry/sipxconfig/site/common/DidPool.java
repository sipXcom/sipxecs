package org.sipfoundry.sipxconfig.site.common;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Parameter;
import org.sipfoundry.sipxconfig.components.TapestryUtils;

@ComponentClass(allowBody = false, allowInformalParameters = false)
public abstract class DidPool extends BaseComponent {

    @Parameter(required = true)
    public abstract String getLabel();
    
    @Parameter(required = true)
    public abstract String getStart();

    public abstract void setStart(String start);

    @Parameter(required = true)
    public abstract String getEnd();

    public abstract void setEnd(String start);
    
    @Parameter(required = true)
    public abstract String getType();

    public abstract void setType(String type);    
    
    @Override
    protected void renderComponent(IMarkupWriter writer, IRequestCycle cycle) {
        if (!TapestryUtils.isRewinding(cycle, this)) {
            /*Date datetime = getDatetime();
            setDate(new Date(datetime.getTime()));

            Calendar calendar = Calendar.getInstance(getPage().getLocale());
            calendar.setTime(datetime);
            setTime(new TimeOfDay(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));*/
        }

        super.renderComponent(writer, cycle);

        if (TapestryUtils.isRewinding(cycle, this) && TapestryUtils.isValid(this)) {
            /*Date  datetime = toDateTime(getDate(), getTime(), getPage().getLocale());
            setDatetime(datetime);*/
        }
    }    
}
