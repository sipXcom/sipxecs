package org.sipfoundry.sipxconfig.site.common;

import java.util.Collection;
import java.util.Map;

import org.apache.hivemind.Location;
import org.apache.hivemind.Messages;
import org.apache.tapestry.IAsset;
import org.apache.tapestry.IBeanProvider;
import org.apache.tapestry.IBinding;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRender;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.components.Block;
import org.apache.tapestry.engine.IPageLoader;
import org.apache.tapestry.internal.event.IComponentEventInvoker;
import org.apache.tapestry.listener.ListenerMap;
import org.apache.tapestry.spec.IComponentSpecification;

public class ComparableComponent extends Block implements Comparable<IComponent> {

    IComponent m_component;
    
    public ComparableComponent(IComponent component) {
        m_component = component;
    }
    @Override
    public int compareTo(IComponent component) {        
        return m_component.getClientId().compareTo(component.getClientId());
    }

    @Override
    public void setLocation(Location arg0) {
        m_component.setLocation(arg0);
        
    }
    @Override
    public Location getLocation() {        
        return m_component.getLocation();
    }
    @Override
    public void addAsset(String arg0, IAsset arg1) {
        m_component.addAsset(arg0, arg1);
        
    }
    @Override
    public void addBody(IRender arg0) {
        m_component.addBody(arg0);
        
    }
    @Override
    public void addComponent(IComponent arg0) {
        m_component.addComponent(arg0);
        
    }
    
    @Override
    public void finishLoad(IRequestCycle arg0, IPageLoader arg1, IComponentSpecification arg2) {
        m_component.finishLoad(arg0, arg1, arg2);
        
    }
    @Override
    public IAsset getAsset(String arg0) {
        return m_component.getAsset(arg0);
    }
    @Override
    public Map getAssets() {        
        return m_component.getAssets();
    }
    @Override
    public IBeanProvider getBeans() {
        return m_component.getBeans();
    }
    @Override
    public IBinding getBinding(String arg0) {        
        return m_component.getBinding(arg0);
    }
    @Override
    public Collection getBindingNames() {        
        return m_component.getBindingNames();
    }
    @Override
    public Map getBindings() {
        return m_component.getBindings();
    }
    @Override
    public String getClientId() {
        return m_component.getClientId();
    }
    @Override
    public IComponent getComponent(String arg0) {
        return m_component.getComponent(arg0);
    }
    @Override
    public Map getComponents() {
        return m_component.getComponents();
    }

    @Override
    public IComponent getContainer() {        
        return m_component.getContainer();
    }
    @Override
    public IComponentEventInvoker getEventInvoker() {
        return m_component.getEventInvoker();
    }
    @Override
    public String getExtendedId() {
        return m_component.getExtendedId();
    }
    @Override
    public String getId() {
        return m_component.getId();
    }
    @Override
    public String getIdPath() {
        return m_component.getIdPath();
    }
    @Override
    public ListenerMap getListeners() {
        return m_component.getListeners();
    }
    @Override
    public Messages getMessages() {
        return m_component.getMessages();
    }
    @Override
    public INamespace getNamespace() {
        return m_component.getNamespace();
    }
    @Override
    public IPage getPage() {
        return m_component.getPage();
    }
    @Override
    public IComponentSpecification getSpecification() {
        return m_component.getSpecification();
    }
    @Override
    public String getSpecifiedId() {
        return m_component.getSpecifiedId();
    }
    @Override
    public String getTemplateTagName() {
        return m_component.getTemplateTagName();
    }

    @Override
    public String peekClientId() {
        return m_component.peekClientId();
    }
    @Override
    public void renderBody(IMarkupWriter arg0, IRequestCycle arg1) {
        m_component.renderBody(arg0, arg1);
        
    }
    @Override
    public void setBinding(String arg0, IBinding arg1) {
        m_component.setBinding(arg0, arg1);
        
    }
    @Override
    public void setClientId(String arg0) {
        m_component.setClientId(arg0);
        
    }

    @Override
    public void setContainer(IComponent arg0) {
        m_component.setContainer(arg0);
        
    }
    @Override
    public void setId(String arg0) {
        m_component.setId(arg0);
        
    }
    @Override
    public void setNamespace(INamespace arg0) {
        m_component.setNamespace(arg0);
        
    }
    @Override
    public void setPage(IPage arg0) {
        m_component.setPage(arg0);
    }
    @Override
    public void setTemplateTagName(String arg0) {
        m_component.setTemplateTagName(arg0);
    }

}
