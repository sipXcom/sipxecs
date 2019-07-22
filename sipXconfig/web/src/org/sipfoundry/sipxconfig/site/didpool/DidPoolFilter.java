package org.sipfoundry.sipxconfig.site.didpool;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.site.didpool.DidPoolSearch;
import org.sipfoundry.sipxconfig.components.LocalizedOptionModelDecorator;
import org.sipfoundry.sipxconfig.components.NewEnumPropertySelectionModel;
import org.sipfoundry.sipxconfig.components.TapestryContext;
import org.sipfoundry.sipxconfig.components.selection.OptGroupPropertySelectionRenderer;
import static org.sipfoundry.sipxconfig.site.didpool.DidPoolSearch.Mode;

public abstract class DidPoolFilter extends BaseComponent {
    public abstract boolean getSearchMode();

    @InjectObject(value = "spring:tapestry")
    public abstract TapestryContext getTapestry();

    @Bean
    public abstract OptGroupPropertySelectionRenderer getPropertyRenderer();

    public abstract void setSelectionModel(IPropertySelectionModel model);

    public abstract IPropertySelectionModel getSelectionModel();

    @Parameter(required = true)
    public abstract void setDidPoolSearch(DidPoolSearch didPoolSearch);

    public abstract DidPoolSearch getDidPoolSearch();

    protected void prepareForRender(IRequestCycle cycle) {
        if (getSelectionModel() == null) {
            NewEnumPropertySelectionModel model = new NewEnumPropertySelectionModel();
            model.setEnumType(Mode.class);

            LocalizedOptionModelDecorator decoratedModel = new LocalizedOptionModelDecorator();
            decoratedModel.setMessages(getMessages());
            decoratedModel.setModel(model);
            decoratedModel.setResourcePrefix("filter.");

            setSelectionModel(getTapestry().addExtraOption(decoratedModel, getMessages(),
                    "label.filter"));
        }
    }
}
