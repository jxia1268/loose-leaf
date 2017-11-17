package tmp;

import java.net.URISyntaxException;

public class RestClientLab extends BasicRestClient {
	
	public static void main(String[] args) {
		RestClientLab client = new RestClientLab();
		BasicRestClient.NEED_EXPERIMENT = true;
		client.execute();
	}

	/**
	 * XXX 実験室
	 * @throws URISyntaxException
	 */
	@Override
	protected void gotoLaboratory() throws URISyntaxException {
		
		
		startProcess("processDefinitionKey", "oneTaskProcess");
		//GET	/form/form-data
		
	}


/*
GET	/form/form-data	13.9.1.	Get form data		org.activiti.rest.service.api.form	FormDataResource.java (2 matches)	62			
POST	/form/form-data	13.9.2.	Submit task form data		org.activiti.rest.service.api.form	FormDataResource.java (2 matches)	95			
GET	/management/deadletter-jobs	#N/A	#N/A		org.activiti.rest.service.api.management	DeadLetterJobCollectionResource.java	80			
GET	/management/jobs	13.13.5.	Get a list of jobs		org.activiti.rest.service.api.management	JobCollectionResource.java	79			
GET 	/management/jobs/{jobId}/exception-stacktrace	#N/A	#N/A		org.activiti.rest.service.api.management	JobExceptionStacktraceResource.java (4 matches)	50			
GET 	/management/timer-jobs/{jobId}/exception-stacktrace	#N/A	#N/A		org.activiti.rest.service.api.management	JobExceptionStacktraceResource.java (4 matches)	72			
GET 	/management/suspended-jobs/{jobId}/exception-stacktrace	#N/A	#N/A		org.activiti.rest.service.api.management	JobExceptionStacktraceResource.java (4 matches)	94			
GET 	/management/deadletter-jobs/{jobId}/exception-stacktrace	#N/A	#N/A		org.activiti.rest.service.api.management	JobExceptionStacktraceResource.java (4 matches)	116			
GET	/management/jobs/{jobId}	13.13.1.	Get a single job		org.activiti.rest.service.api.management	JobResource.java (8 matches)	61			
GET	/management/timer-jobs/{jobId}	#N/A	#N/A		org.activiti.rest.service.api.management	JobResource.java (8 matches)	77			
GET	/management/suspended-jobs/{jobId}	#N/A	#N/A		org.activiti.rest.service.api.management	JobResource.java (8 matches)	93			
GET	/management/deadletter-jobs/{jobId}	#N/A	#N/A		org.activiti.rest.service.api.management	JobResource.java (8 matches)	109			
DELETE 	/management/jobs/{jobId}	#N/A	#N/A		org.activiti.rest.service.api.management	JobResource.java (8 matches)	125			
DELETE 	/management/timer-jobs/{jobId}	#N/A	#N/A		org.activiti.rest.service.api.management	JobResource.java (8 matches)	141			
DELETE 	/management/deadletter-jobs/{jobId}	#N/A	#N/A		org.activiti.rest.service.api.management	JobResource.java (8 matches)	157			
POST 	/management/jobs/{jobId}	#N/A	#N/A		org.activiti.rest.service.api.management	JobResource.java (8 matches)	174			
GET	/management/engine	13.11.2.	Get engine info		org.activiti.rest.service.api.management	ProcessEngineResource.java	47			
GET	/management/properties	13.11.1.	Get engine properties		org.activiti.rest.service.api.management	PropertiesCollectionResource.java	44			
GET	/management/suspended-jobs	#N/A	#N/A		org.activiti.rest.service.api.management	SuspendedJobCollectionResource.java	80			
GET	/management/tables	13.10.1.	List of tables		org.activiti.rest.service.api.management	TableCollectionResource.java	50			
GET	/management/tables/{tableName}/columns	13.10.3.	Get column info for a single table		org.activiti.rest.service.api.management	TableColumnsResource.java	47			
GET	/management/tables/{tableName}/data	13.10.4.	Get row data for a single table		org.activiti.rest.service.api.management	TableDataResource.java	67			
GET	/management/tables/{tableName}	13.10.2.	Get a single table		org.activiti.rest.service.api.management	TableResource.java	55			
GET	/management/timer-jobs	#N/A	#N/A		org.activiti.rest.service.api.management	TimerJobCollectionResource.java	79			

*/
}
