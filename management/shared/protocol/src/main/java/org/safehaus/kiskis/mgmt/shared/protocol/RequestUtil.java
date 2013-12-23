package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/23/13
 * Time: 3:11 PM
 */
public class RequestUtil {
    public static Task createTask(CommandManagerInterface manager, String description){
        Task task = new Task();
        task.setTaskStatus(TaskStatus.NEW);
        task.setDescription(description);
        if (manager != null) {
            manager.saveTask(task);
        }

        return task;
    }

    public static Request createRequest(CommandManagerInterface manager, final String command, HashMap<String, String> map) {
        String json = command;

        for(String key : map.keySet()){
            json = json.replaceAll(key, map.get(key));
        }


        Request request = CommandJson.getRequest(json);
        if (manager != null) {
            manager.executeCommand(new Command(request));
        }

        return request;
    }
}
