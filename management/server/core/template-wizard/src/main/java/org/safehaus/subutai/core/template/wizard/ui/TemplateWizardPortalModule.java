package org.safehaus.subutai.core.template.wizard.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


/**
 * Created by talas on 2/10/15.
 */
public class TemplateWizardPortalModule implements PortalModule
{
    private final static String MODULE_IMAGE = "magic.gif";
    private final static String MODULE_NAME = "Template Wizard";

    private TemplateRegistry templateRegistry;
    private PeerManager peerManager;
    private Tracker tracker;
    private ExecutorService executor;


    public Tracker getTracker()
    {
        return tracker;
    }


    public TemplateRegistry getTemplateRegistry()
    {
        return templateRegistry;
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public TemplateWizardPortalModule( final TemplateRegistry templateRegistry, final PeerManager peerManager,
                                       final Tracker tracker )
    {
        this.executor = Executors.newSingleThreadExecutor();
        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.tracker = tracker;
    }


    @Override
    public String getId()
    {
        return MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new TemplateWizardComponent( this );
    }


    /**
     * Function to differentiate core plugins from plugins needed to show-up in different tabs in main dashboard
     */
    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }
}