package org.safehaus.subutai.core.configuration.impl.utils;


import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.safehaus.subutai.core.configuration.api.ConfiguraitonTypeEnum;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.gson.JsonObject;


/**
 * @author dilshat
 */
public class IniParser implements ConfigParser
{

    private final PropertiesConfiguration config;


    public IniParser( String content ) throws ConfigurationException
    {
        config = new PropertiesConfiguration();
        config.load( new ByteArrayInputStream( content.getBytes() ) );
    }


    public PropertiesConfiguration getConfig()
    {
        return config;
    }


    //    @Override
    public Object getProperty( String propertyName )
    {
        return config.getString( propertyName );
    }


    public String getStringProperty( String propertyName )
    {
        return config.getString( propertyName );
    }


    public void setProperty( String propertyName, Object propertyValue )
    {
        config.setProperty( propertyName, propertyValue );
    }


    public void addProperty( String propertyName, Object propertyValue )
    {
        config.addProperty( propertyName, propertyValue );
    }


    public String getIni() throws ConfigurationException
    {
        StringWriter str = new StringWriter();
        config.save( str );
        return str.toString();
    }


    @Override
    public JsonObject parserConfig( String pathToConfig, ConfiguraitonTypeEnum configuraitonTypeEnum )
    {
        ConfigBuilder configBuilder = new ConfigBuilder();
        JsonObject jo = configBuilder.getConfigJsonObject( pathToConfig, configuraitonTypeEnum );

        Iterator<String> iterator = config.getKeys();
        List<JsonObject> fields = new ArrayList<>();
        while ( iterator.hasNext() )
        {
            String key = iterator.next();
            String value = ( String ) config.getProperty( key );
            JsonObject field =
                    configBuilder.buildFieldJsonObject( key.trim(), key.trim(), true, "textfield", true, value.trim() );
            fields.add( field );
        }

        JsonObject njo = configBuilder.addJsonArrayToConfig( jo, fields );

        return njo;
    }


    public void setValuesFromJsonObject( JsonObject jsonObject )
    {
        // TODO complete setValues logic
    }
}