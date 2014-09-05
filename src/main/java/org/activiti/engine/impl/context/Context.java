/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.context;

import java.util.Stack;

import org.activiti.engine.ControlParam;
import org.activiti.engine.impl.TaskContext;
import org.activiti.engine.impl.bpmn.behavior.FlowNodeActivityBehavior;
import org.activiti.engine.impl.cfg.BeansConfigurationHelper;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobExecutorContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.apache.log4j.Logger;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class Context {

  protected static ThreadLocal<Stack<CommandContext>> commandContextThreadLocal = new ThreadLocal<Stack<CommandContext>>();
  protected static ThreadLocal<Stack<ProcessEngineConfigurationImpl>> processEngineConfigurationStackThreadLocal = new ThreadLocal<Stack<ProcessEngineConfigurationImpl>>();
  protected static ThreadLocal<Stack<ExecutionContext>> executionContextStackThreadLocal = new ThreadLocal<Stack<ExecutionContext>>();
  protected static ThreadLocal<JobExecutorContext> jobExecutorContextThreadLocal = new ThreadLocal<JobExecutorContext>();
  private static Logger log = Logger.getLogger(Context.class);
  public static boolean enableMixMultiUserTask()
  {
	  return BeansConfigurationHelper.getProcessEngineConfiguration().enableMixMultiUserTask();
  }
  public static CommandContext getCommandContext() {
    Stack<CommandContext> stack = getStack(commandContextThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  public static void setCommandContext(CommandContext commandContext) {
    getStack(commandContextThreadLocal).push(commandContext);
  }

  public static void removeCommandContext() {
    getStack(commandContextThreadLocal).pop();
  }

  public static ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    Stack<ProcessEngineConfigurationImpl> stack = getStack(processEngineConfigurationStackThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  public static void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    getStack(processEngineConfigurationStackThreadLocal).push(processEngineConfiguration);
  }

  public static void removeProcessEngineConfiguration() {
    getStack(processEngineConfigurationStackThreadLocal).pop();
  }

  public static ExecutionContext getExecutionContext() {
    return getStack(executionContextStackThreadLocal).peek();
  }

  public static void setExecutionContext(InterpretableExecution execution) {
    getStack(executionContextStackThreadLocal).push(new ExecutionContext(execution));
  }

  public static void removeExecutionContext() {
    getStack(executionContextStackThreadLocal).pop();
  }

  protected static <T> Stack<T> getStack(ThreadLocal<Stack<T>> threadLocal) {
    Stack<T> stack = threadLocal.get();
    if (stack==null) {
      stack = new Stack<T>();
      threadLocal.set(stack);
    }
    return stack;
  }
  
  public static JobExecutorContext getJobExecutorContext() {
    return jobExecutorContextThreadLocal.get();
  }
  
  public static void setJobExecutorContext(JobExecutorContext jobExecutorContext) {
    jobExecutorContextThreadLocal.set(jobExecutorContext);
  }
  
  public static void removeJobExecutorContext() {
    jobExecutorContextThreadLocal.remove();
  }
  
  public static TaskContext createTaskContext(ExecutionEntity execution,String taskKey)
  {
	  TaskContext taskContext = new TaskContext();
	  try {
	  		ControlParam controlParam = Context.getProcessEngineConfiguration().getKPIService().getControlParam(execution,taskKey);
			taskContext.setControlParam(controlParam);//设定当前任务的控制变量参数
			if(Context.enableMixMultiUserTask() )
    		{
				String users =((FlowNodeActivityBehavior) execution.getActivity().getActivityBehavior()).getAssignee(null, execution);
	 		
				if(users == null || users.indexOf(",") < 0)
				{
					taskContext.setOneassignee(true);
				}
				else
					taskContext.setOneassignee(false);
    		}
	  } catch (Exception e) {
			
			log.error("",e);
		}
			return taskContext;
			
  }
  
  public static void createTaskContextControlParam(TaskContext taskContext,ExecutionEntity execution,String taskKey)
  {
	  try {
	  		ControlParam controlParam = Context.getProcessEngineConfiguration().getKPIService().getControlParam(execution,taskKey);
			taskContext.setControlParam(controlParam);//设定当前任务的控制变量参数
			if(Context.enableMixMultiUserTask() )
    		{
				String users =((FlowNodeActivityBehavior) execution.getActivity().getActivityBehavior()).getAssignee(null, execution);
	 		
				if(users == null || users.indexOf(",") < 0)
				{
					taskContext.setOneassignee(true);
				}
				else
					taskContext.setOneassignee(false);
    		}
	  } catch (Exception e) {
			
			log.error("",e);
		}
			
  }
}
