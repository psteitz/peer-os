package io.subutai.core.environment.api;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentCreationRef;
import io.subutai.common.environment.EnvironmentDto;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.Topology;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.protocol.ReverseProxyConfig;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKeys;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;


public interface EnvironmentManager
{

    String getEnvironmentOwnerNameById( long userId );

    /**
     * Returns a set of DTO objects of all local environments
     * Used by users with Tenant-Management role
     */
    Set<EnvironmentDto> getTenantEnvironments();


    /**
     * Returns all existing environments
     *
     * @return - set of {@code Environment}
     */
    Set<Environment> getEnvironments();

    Set<Environment> getEnvironmentsByOwnerId( long userId );


    EnvironmentCreationRef createEnvironment( Topology topology, boolean async ) throws EnvironmentCreationException;

    //used in plugins, kept for backward compatibility
    Set<EnvironmentContainerHost> growEnvironment( final String environmentId, final Topology topology,
                                                   final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    EnvironmentCreationRef modifyEnvironment( String environmentId, Topology topology, List<String> removedContainers,
                                              Map<String, ContainerSize> changedContainers, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException;


    /**
     * Assigns ssh key to environment and inserts it into authorized_keys file of all the containers within the
     * environment
     *
     * @param environmentId - environment id
     * @param sshKey - ssh key content
     * @param async - indicates whether ssh key is added synchronously or asynchronously to the calling party
     */
    void addSshKey( String environmentId, String sshKey, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException;

    /**
     * Removes ssh key from environment containers authorized_keys file
     *
     * @param environmentId - environment id
     * @param sshKey - ssh key content
     * @param async - indicates whether ssh key is removed synchronously or asynchronously to the calling party
     */
    void removeSshKey( String environmentId, String sshKey, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException;

    /**
     * Returns ssh keys of environment containers
     *
     * @param environmentId environment id
     * @param encType encription type
     *
     * @return ssh keys
     */
    SshKeys getSshKeys( String environmentId, SshEncryptionType encType );


    /**
     * Generates ssh key with given encryption type
     *
     * @param environmentId environment id
     * @param encType rsa or dsa
     *
     * @return ssh public key
     */
    SshKeys createSshKey( String environmentId, String hostname, SshEncryptionType encType );

    /**
     * Allows to change p2p network's secret key
     *
     * @param environmentId - environment id
     * @param newP2pSecretKey - new secret key
     * @param p2pSecretKeyTtlSec - new secret key's time-to-live in seconds
     */
    void resetP2PSecretKey( String environmentId, String newP2pSecretKey, long p2pSecretKeyTtlSec, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException;

    /**
     * Destroys environment by id.
     *
     * @param environmentId - environment id
     * @param async - indicates whether environment is destroyed synchronously or asynchronously to the calling party
     * containers were destroyed, otherwise an exception is thrown when first error occurs
     *
     * @throws EnvironmentDestructionException - thrown if error occurs during environment destruction
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    void destroyEnvironment( String environmentId, boolean async )
            throws EnvironmentDestructionException, EnvironmentNotFoundException;


    /**
     * Destroys container. If this is the last container, the associated environment will be removed too
     *
     * @param environmentId - id of container environment
     * @param containerId - id of container to destroy
     * @param async - indicates whether container is destroyed synchronously or asynchronously to the calling party was
     * not destroyed due to some error, otherwise an exception is thrown
     *
     * @throws EnvironmentModificationException - thrown if error occurs during environment modification
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    void destroyContainer( String environmentId, String containerId, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Cancels active workflow for the specified environment
     *
     * @param environmentId id of environment
     *
     * @throws EnvironmentManagerException if exception is thrown during cancellation
     */
    void cancelEnvironmentWorkflow( final String environmentId ) throws EnvironmentManagerException;

    Map<String, CancellableWorkflow> getActiveWorkflows();

    /**
     * Returns environment by id
     *
     * @param environmentId - environment id
     *
     * @return - {@code Environment}
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    Environment loadEnvironment( String environmentId ) throws EnvironmentNotFoundException;


    /**
     * Removes an assigned domain if any from the environment
     *
     * @param environmentId - id of the environment which domain to remove
     */
    void removeEnvironmentDomain( String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Assigns a domain to the environment. External client would be able to access the environment containers via the
     * domain name.
     *
     * @param environmentId - id of the environment to assign the passed domain to
     * @param newDomain - domain url
     * @param proxyLoadBalanceStrategy - strategy to load balance requests to the domain
     * @param sslCertPath - path to SSL certificate to enable HTTPS access to domain only, null if not needed
     */
    void assignEnvironmentDomain( String environmentId, String newDomain,
                                  ProxyLoadBalanceStrategy proxyLoadBalanceStrategy, String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Returns the currently assigned domain
     *
     * @param environmentId - id of the environment which domain to return
     *
     * @return - domain url or null if not assigned
     */
    String getEnvironmentDomain( String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException;


    boolean isContainerInEnvironmentDomain( String containerHostId, String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException;


    void addContainerToEnvironmentDomain( String containerHostId, String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException;

    /**
     * Sets up ssh connectivity for container. Clients can connect to the container via ssh during 30 seconds after this
     * call. Connection will remain active unless client is idle for 30 seconds.
     *
     * @param containerHostId container id
     * @param environmentId env id
     *
     * @return port for ssh connection
     */
    SshTunnel setupSshTunnelForContainer( String containerHostId, String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException;

    void removeContainerFromEnvironmentDomain( String containerHostId, String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException;

    void notifyOnContainerDestroyed( Environment environment, String containerId );

    void notifyOnEnvironmentDestroyed( final String environmentId );

    void addAlertHandler( AlertHandler alertHandler );

    void removeAlertHandler( AlertHandler alertHandler );

    Collection<AlertHandler> getRegisteredAlertHandlers();

    EnvironmentAlertHandlers getEnvironmentAlertHandlers( EnvironmentId environmentId )
            throws EnvironmentNotFoundException;


    void startMonitoring( String handlerId, AlertHandlerPriority handlerPriority, String environmentId )
            throws EnvironmentManagerException;

    void stopMonitoring( String handlerId, AlertHandlerPriority handlerPriority, String environmentId )
            throws EnvironmentManagerException;

    void addReverseProxy( final Environment environment, final ReverseProxyConfig reverseProxyConfig )
            throws EnvironmentModificationException;


    void changeContainerHostname( final ContainerId containerId, final String newHostname, final boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException;

    /**
     * Adds ssh key to environment db entity. This method does not add the key to containers. This is a workaround to
     * allow Hub module to modify list of ssh keys inside environment db record. After call to this method, caller
     * should reload environment using EnvironmentManager@loadEnvironment
     */
    void addSshKeyToEnvironmentEntity( String environmentId, String sshKey ) throws EnvironmentNotFoundException;

    void excludePeerFromEnvironment( String environmentId, String peerId )
            throws EnvironmentNotFoundException, EnvironmentManagerException;
}
