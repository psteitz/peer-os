/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.agentmanager;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentListener;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class AgentManagerImplTest {

    private AgentManager agentManager;
    private CommunicationManagerFake communicationManager;

    public AgentManagerImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        agentManager = new AgentManagerImpl();
        communicationManager = new CommunicationManagerFake();
        ((AgentManagerImpl) agentManager).setCommunicationService(communicationManager);
        ((AgentManagerImpl) agentManager).init();
    }

    @After
    public void tearDown() {
        ((AgentManagerImpl) agentManager).destroy();
    }

    private Response getRegistrationRequestResponse(UUID uuid, boolean isLxc, String hostname) {
        Response response = new Response();
        response.setUuid(uuid);
        response.setIsLxc(isLxc);
        response.setHostname(hostname);
        response.setIps(new ArrayList<String>());
        response.setType(ResponseType.REGISTRATION_REQUEST);
        return response;
    }

    @Test
    public void testRegistrationWithCommunication() {
        assertTrue(communicationManager.isIsListenerAdded());
    }

    @Test
    public void testUnRegistrationFRomCommunication() {
        ((AgentManagerImpl) agentManager).destroy();
        assertTrue(communicationManager.isIsListenerRemoved());
    }

    @Test
    public void testGetAgentsEmpty() {
        assertTrue(agentManager.getAgents().isEmpty());
    }

    @Test
    public void testGetPhysicalAgentsEmpty() {
        assertTrue(agentManager.getPhysicalAgents().isEmpty());
    }

    @Test
    public void testGetLxcAgentsEmpty() {
        assertTrue(agentManager.getLxcAgents().isEmpty());
    }

    @Test
    public void testGetAgentsNotEmpty() {

        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(UUID.randomUUID(), false, null));
        assertFalse(agentManager.getAgents().isEmpty());
    }

    @Test
    public void testGetPhysicalAgentsEmpty2() {
        UUID uuid = UUID.randomUUID();
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(uuid, true, null));
        assertTrue(agentManager.getPhysicalAgents().isEmpty());
    }

    @Test
    public void testGetPhysicalAgentsNotEmpty() {
        UUID uuid = UUID.randomUUID();
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(uuid, false, null));
        assertFalse(agentManager.getPhysicalAgents().isEmpty());
    }

    @Test
    public void testGetLxcAgentsEmpty2() {
        UUID uuid = UUID.randomUUID();
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(uuid, false, null));
        assertTrue(agentManager.getLxcAgents().isEmpty());
    }

    @Test
    public void testGetLxcAgentsNotEmpty() {
        UUID uuid = UUID.randomUUID();
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(uuid, true, null));
        assertFalse(agentManager.getLxcAgents().isEmpty());
    }

    @Test
    public void testGetAgentsByMissingHostname() {
        UUID uuid = UUID.randomUUID();
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(uuid, true, null));
        assertTrue(agentManager.getAgents().iterator().next().getHostname().equals(uuid.toString()));
    }

    @Test
    public void testGetAgentsByPresentHostname() {
        UUID uuid = UUID.randomUUID();
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(uuid, true, "hostname"));
        assertTrue(agentManager.getAgents().iterator().next().getHostname().equals("hostname"));
    }

    @Test
    public void testGetAgentsByHostname() {
        UUID uuid = UUID.randomUUID();
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(uuid, true, "hostname"));
        assertNotNull(agentManager.getAgentByHostname("hostname"));
    }

    @Test
    public void testGetAgentsByPresentUUID() {
        UUID uuid = UUID.randomUUID();
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(uuid, false, null));
        assertTrue(agentManager.getAgents().iterator().next().getUuid().equals(uuid));
    }

    @Test
    public void testGetAgentsByMissingUUID() {
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(null, false, null));
        assertTrue(agentManager.getAgents().isEmpty());
    }

    @Test
    public void testGetAgentsByUUID() {
        UUID uuid = UUID.randomUUID();
        ((ResponseListener) agentManager).onResponse(getRegistrationRequestResponse(uuid, false, null));
        assertNotNull(agentManager.getAgentByUUID(uuid));
    }

    @Test
    public void testGetAgentsByParentHostname() {
        UUID uuid = UUID.randomUUID();
        Response response = getRegistrationRequestResponse(uuid, true, String.format("hostname%ssomesuffix", Common.PARENT_CHILD_LXC_SEPARATOR));
        ((ResponseListener) agentManager).onResponse(response);
        assertNotNull(agentManager.getLxcAgentsByParentHostname("hostname"));
    }

    @Test
    public void testSendAckToAgent() {
        UUID uuid = UUID.randomUUID();
        Response response = getRegistrationRequestResponse(uuid, true, null);
        ((ResponseListener) agentManager).onResponse(response);
        assertEquals(RequestType.REGISTRATION_REQUEST_DONE, communicationManager.getRequest().getType());
    }

    @Test
    public void testSendAckToAgentUUID() {
        UUID uuid = UUID.randomUUID();
        Response response = getRegistrationRequestResponse(uuid, true, null);
        ((ResponseListener) agentManager).onResponse(response);
        assertEquals(uuid, communicationManager.getRequest().getUuid());
    }

    @Test
    public void testDeleteAgent() {
        UUID uuid = UUID.randomUUID();
        Response response = getRegistrationRequestResponse(uuid, true, null);
        response.setTransportId("blablabla");
        ((ResponseListener) agentManager).onResponse(response);
        assertFalse(agentManager.getAgents().isEmpty());
        response.setType(ResponseType.AGENT_DISCONNECT);
        ((ResponseListener) agentManager).onResponse(response);
        assertTrue(agentManager.getAgents().isEmpty());
    }

    @Test
    public void testAddListener() {
        agentManager.addListener(new AgentListener() {

            public void onAgent(Set<Agent> freshAgents) {

            }
        });

        assertFalse(((AgentManagerImpl) agentManager).getListeners().isEmpty());
    }

    @Test
    public void testRemoveListener() {
        AgentListener listener = new AgentListener() {

            public void onAgent(Set<Agent> freshAgents) {

            }
        };
        agentManager.addListener(listener);

        agentManager.removeListener(listener);

        assertTrue(((AgentManagerImpl) agentManager).getListeners().isEmpty());
    }

}
