package org.safehaus.subutai.core.registry.impl;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@Ignore
public class TemplateDAOTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateDAOTest.class.getName() );

    //    private EntityManager em;
    private EntityManagerFactory emf;
    //    private EntityTransaction tx;

    private TemplateDAO templateDAO;


    @Before
    public void setUp() throws Exception
    {
        emf = Persistence.createEntityManagerFactory( "default" );
        templateDAO = new TemplateDAO();
        templateDAO.setEntityManagerFactory( emf );
    }


    @After
    public void tearDown() throws Exception
    {
        //        em.close();
        emf.close();
    }


    @Test
    public void testSaveTemplate() throws Exception
    {
        Template template = TestUtils.getParentTemplate();
        templateDAO.saveTemplate( template );
        Template savedTemplate = templateDAO.getTemplateByName( template.getTemplateName(), template.getLxcArch() );
        assertEquals( template, savedTemplate );
    }


    @Test
    public void testGetAllTemplates() throws Exception
    {
        Map<Pair<String, String>, Template> templates = new HashMap<>();

        Template childTemplate = TestUtils.getChildTemplate();
        Template parentTemplate = TestUtils.getParentTemplate();

        templates.put( new ImmutablePair<>( childTemplate.getTemplateName(), childTemplate.getLxcArch() ),
                childTemplate );
        templates.put( new ImmutablePair<>( parentTemplate.getTemplateName(), parentTemplate.getLxcArch() ),
                parentTemplate );

        //        templates.add( TestUtils.getChildTemplate() );
        //        templates.add( TestUtils.getParentTemplate() );

        LOGGER.info( "Templates going to be persisted" );
        for ( Map.Entry<Pair<String, String>, Template> pairTemplateEntry : templates.entrySet() )
        {
            templateDAO.saveTemplate( pairTemplateEntry.getValue() );
            LOGGER.warn( pairTemplateEntry.getValue().getTemplateName() );
        }


        LOGGER.info( "Templates persisted in database" );
        Map<Pair<String, String>, Template> savedTemplatesMap = new HashMap<>();
        List<Template> savedTemplates = templateDAO.getAllTemplates();
        for ( Template savedTemplate : savedTemplates )
        {
            savedTemplatesMap.put( new ImmutablePair<>( savedTemplate.getTemplateName(), savedTemplate.getLxcArch() ),
                    savedTemplate );
            LOGGER.warn( savedTemplate.getTemplateName() );
        }

        assertArrayEquals( templates.entrySet().toArray(), savedTemplatesMap.entrySet().toArray() );
    }


    @Test
    public void testRemoveTemplate() throws Exception
    {
        LOGGER.warn( "\n\n\n\nTesting Remove Template" );
        Template template = TestUtils.getParentTemplate();
        templateDAO.saveTemplate( template );

        Template savedTemplate = templateDAO.getTemplateByName( template.getTemplateName(), template.getLxcArch() );
        LOGGER.warn( template.toString() );
        LOGGER.warn( savedTemplate.toString() );
        LOGGER.warn( "\n\n\n\nGetting all templates from DB" );
        for ( Template template1 : templateDAO.getAllTemplates() )
        {
            LOGGER.warn( template1.getTemplateName() );
        }
        assertEquals( template, savedTemplate );
        templateDAO.removeTemplate( template );
        savedTemplate = templateDAO.getTemplateByName( template.getTemplateName(), template.getLxcArch() );
        LOGGER.warn( "\n\n\n\nGetting template from database" );
        LOGGER.warn( savedTemplate.toString() );
        assertNotNull( savedTemplate );
    }


    @Test
    public void testGetChildTemplates() throws Exception
    {
        Template parentTemplate = TestUtils.getParentTemplate();
        Template childTemplate = TestUtils.getChildTemplate();
        parentTemplate.addChildren( Arrays.asList( childTemplate ) );
        templateDAO.saveTemplate( parentTemplate );
        List<Template> childTemplates =
                templateDAO.getChildTemplates( parentTemplate.getTemplateName(), parentTemplate.getLxcArch() );
        assertTrue( childTemplates.contains( childTemplate ) );
    }


    @Test
    public void testGetTemplateByName() throws Exception
    {
        Template template = TestUtils.getParentTemplate();
        templateDAO.saveTemplate( template );
        Template savedTemplate = templateDAO.getTemplateByName( template.getTemplateName(), template.getLxcArch() );
        assertEquals( template, savedTemplate );
    }
}