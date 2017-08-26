package io.subutai.core.hubmanager.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.hubmanager.api.model.ContainerMetrics;


/**
 * Implementation of Container host metrics DAO
 */
class ContainerMetricsDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerMetrics.class );

    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    ContainerMetricsDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    List<ContainerMetrics> getAll()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<ContainerMetrics> result = Lists.newArrayList();
        try
        {
            daoManager.startTransaction( em );

            result = em.createQuery( "select cm from ContainerMetricsEntity cm", ContainerMetrics.class )
                       .getResultList();

            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    void persist( ContainerMetrics item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );

            em.persist( item );
            em.flush();

            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    void remove( final Long id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );

            ContainerMetrics item = em.find( ContainerMetrics.class, id );
            em.remove( item );

            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }
}
