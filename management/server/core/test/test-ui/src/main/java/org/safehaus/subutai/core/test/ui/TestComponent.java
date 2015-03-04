package org.safehaus.subutai.core.test.ui;


import java.util.UUID;

import org.safehaus.subutai.core.test.api.Test;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;


public class TestComponent extends CustomComponent
{

    public TestComponent( final Test test )
    {

        VerticalLayout layout = new VerticalLayout();


        Button logBtn = new Button( "Log user" );
        logBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                test.logUsername();
            }
        } );

        Button showBtn = new Button( "Show user" );
        showBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Notification.show( "USER >>>" + test.getUserName() );
            }
        } );

        Button loginWithTokenBtn = new Button( "Login with token" );
        loginWithTokenBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Notification.show( test.loginWithToken( UUID.randomUUID().toString(), "127.0.0.1" ).toString() );
            }
        } );

        layout.addComponent( logBtn );
        layout.addComponent( showBtn );
        layout.addComponent( loginWithTokenBtn );

        setCompositionRoot( layout );
    }
}
