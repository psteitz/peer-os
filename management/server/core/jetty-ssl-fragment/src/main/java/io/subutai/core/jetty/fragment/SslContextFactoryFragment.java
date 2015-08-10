package io.subutai.core.jetty.fragment;


import java.security.KeyStore;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SslContextFactoryFragment extends SslContextFactory
{
    private static Logger LOG = LoggerFactory.getLogger( SslContextFactoryFragment.class.getName() );

    private static volatile SslContextFactoryFragment lastInstance = new SslContextFactoryFragment();

    private boolean customStart = false;

    private String _keyStorePassword = "subutai";
    private String _trustStorePassword = "subutai";


    public SslContextFactoryFragment()
    {
        super();
        lastInstance = this;
    }


    public static SslContextFactoryFragment getLastInstance()
    {
        return lastInstance;
    }


    @Override
    protected void doStop() throws Exception
    {
        if ( customStart )
        {
            stop();
        }
        else
        {
            super.doStop();
        }
    }


    public void reloadStores()
    {
        try
        {
            LOG.debug( "Reloading ssl context factory" );
            setCustomStart( true );
            doStop();
            setCustomStart( false );

            setSslContext( null );

            setKeyStore( ( KeyStore ) null );

            setTrustStore( ( KeyStore ) null );

            setKeyStorePassword( _keyStorePassword );

            setTrustStorePassword( _trustStorePassword );

            doStart();
        }
        catch ( Exception e )
        {
            LOG.error( "Error restarting ssl context factory", e );
        }
    }


    public void setCustomStart( final boolean customStart )
    {
        this.customStart = customStart;
    }
}