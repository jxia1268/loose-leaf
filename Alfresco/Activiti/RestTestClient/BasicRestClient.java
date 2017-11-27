/**
 * 
 */
package tmp;


import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

@SuppressWarnings("rawtypes")
public class BasicRestClient {
	protected static boolean NEED_EXPERIMENT = true;//XXX SWITCH METHODS HERE!!

	protected static final String URL_ROOT = "http://localhost:8081/activiti-webapp-rest2/service";
	protected static final String URL_ID_USERS = "/identity/users";
	protected static final String URL_RE_PROCESS_DEFINITIONS = "/repository/process-definitions";
	protected static final String URL_RU_PROCESS_INSTANCES = "/runtime/process-instances";
	protected static final String URL_RU_TASKS = "/runtime/tasks";
	protected static final String URL_HI_PROCESS_INSTANCES = "/history/historic-process-instances";
	protected static final String URL_HI_TASK_INSTANCES = "/history/historic-task-instances";
	

	protected static final String SIGN_METHOD_START = "### ";
	protected static final String SIGN_MARKABLE_STATUS = ">> ";

	protected RestTemplate restTemplate = new RestTemplate();//can be injected
	protected String methodName;

	
	// available users:
	// kermit:kermit --> should be "Basic a2VybWl0Omtlcm1pdA=="
	// gonzo:gonzo
	// fozzie:fozzie
	protected String userId = "kermit";
	protected String userPw = "kermit";

	protected String uniqueStr = createUniqueStrFromCurrentTime();//HHmmで一意文字列を生成する
	
	protected void execute() {

		try {
			if (NEED_EXPERIMENT) {
				gotoLaboratory();
			} else {
				execWholeScenario();
			}

			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * XXX 実験室
	 * @throws URISyntaxException
	 */
	protected void gotoLaboratory() throws URISyntaxException {
		logMethodName();
		System.out.println("Welcome to Rest Client Laboratory");
	}

	@SuppressWarnings("unchecked")
	protected void execWholeScenario() throws URISyntaxException {
		logMethodName();

		LinkedHashMap resBody;
		
		{// ■ ユーザ管理系
			referUsers();
			String tmpUserId = "tmpUser" + uniqueStr;//Activitiのユーザテーブル登録時のPKとなる文字列を引数に指定する
			// 登録
			createUser(tmpUserId);
			// 照会
			referUsers();
			// 更新
			Map<String, String> updateTargets = new HashMap<>();
			updateTargets.put("lastName", "myNewFamilyName");
			updateUser(tmpUserId, updateTargets);
			referUser(tmpUserId);
			// 削除
			deleteUser(tmpUserId);
			referUsers();
		}
		
		// ■ プロセス制御系
		int processInstanceId;
		int activeProcessInstanceCount;

		referProcessDefinitions();//既存プロセス定義の確認
		referProcessDefinition("key", "createTimersProcess");//特定のプロセス定義の確認
		resBody = referProcessInstances();//アクティブなプロセスの確認
		activeProcessInstanceCount = Integer.parseInt(resBody.get("size").toString());
		System.out.println(SIGN_MARKABLE_STATUS + activeProcessInstanceCount + " process(s) is(are) currently active.");
		
		if (activeProcessInstanceCount == 0) {
			//新規プロセスの開始 (ID指定)
			startProcess("processDefinitionId", "oneTaskProcess:1:35");
			resBody = referProcessInstances();//アクティブなプロセスの確認  >> 1つ増えている筈
			activeProcessInstanceCount = Integer.parseInt(resBody.get("size").toString());
			System.out.println(SIGN_MARKABLE_STATUS + activeProcessInstanceCount + " process(s) is(are) currently active.");
		}
		
		ArrayList<LinkedHashMap> activeProcessInstancesList = (ArrayList<LinkedHashMap>) resBody.get("data");
		ArrayList <String> activeProcessDefinitionKeyList = extractFromHashMapList(activeProcessInstancesList, "processDefinitionKey");
		ArrayList <String> activeProcessInstanceIdList = extractFromHashMapList(activeProcessInstancesList, "id");
		processInstanceId = Integer.parseInt(activeProcessInstanceIdList.get(0));//whichever is fine. take 1st one.

		// 特定プロセスインスタンスに対する操作
		if (activeProcessDefinitionKeyList.contains("oneTaskProcess")) {
			//プロセスインスタンスの一時停止・再開
			referProcessInstance(processInstanceId);//特定プロセスの現状確認
			suspendProcessInstance(processInstanceId);//特定プロセスの一時停止
			referProcessInstance(processInstanceId);//確認
			activateProcessInstance(processInstanceId);//特定プロセスの再開
			//プロセスインスタンスの削除
			resBody = referProcessInstances();//アクティブなプロセスの確認  >> 1つ増えている筈
			activeProcessInstanceCount = Integer.parseInt(resBody.get("size").toString());
			System.out.println(SIGN_MARKABLE_STATUS + activeProcessInstanceCount + " process(s) is(are) currently active.");
			deleteProcessInstance(processInstanceId);//特定プロセスの削除
			resBody = referProcessInstances();//アクティブなプロセスの確認  >> 1つ増えている筈
			activeProcessInstanceCount = Integer.parseInt(resBody.get("size").toString());
			System.out.println(SIGN_MARKABLE_STATUS + activeProcessInstanceCount + " process(s) is(are) currently active.");
			//終了済みプロセスインスタンス一覧の照会
			referHistoricProcessInstances();
		}

		if (activeProcessInstanceCount == 0) {
			//新規プロセスの開始 (Key指定)
			startProcess("processDefinitionKey", "oneTaskProcess");
			resBody = referProcessInstances();//アクティブなプロセスの確認  >> 1つ増えている筈
			activeProcessInstanceCount = Integer.parseInt(resBody.get("size").toString());
			System.out.println(SIGN_MARKABLE_STATUS + activeProcessInstanceCount + " process(s) is(are) currently active.");
		}
		
		// ■ タスク制御
		int activeTaskId;
		//既存タスク一覧の照会
		ArrayList<LinkedHashMap> activeTasksList = (ArrayList<LinkedHashMap>) referTasks().get("data");
		ArrayList <String> activeTasksIdList = extractFromHashMapList(activeTasksList, "id");
		activeTaskId = Integer.parseInt(activeTasksIdList.get(0));
		referTask(activeTaskId);//特定タスクの照会
		completeTask(activeTaskId);//特定タスクの終了
		//確認
		referTasks();//既存タスク一覧
		referProcessInstances();//既存プロセスインスタンス一覧
		//終了済みタスクインスタンス一覧の照会
		referHistoricTaskInstances();

	}

	protected void referHistoricTaskInstances() throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "SELECT ALL HISTORIC TASK INSTANCES");
		execRestGetCall(URL_HI_TASK_INSTANCES + "?finished=true");
	}

	protected void completeTask(int taskInstanceId) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "COMPLETE A TASK");
		JSONObject reqBody = new JSONObject();
		reqBody.put("action", "complete");
		reqBody.put("variables", new ArrayList<>());
		
		execRestPostCall(URL_RU_TASKS + "/" + taskInstanceId, reqBody);
	}

	protected void referTask(int taskInstanceId) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "SELECT A SPESIFIC TASK");
		execRestGetCall(URL_RU_TASKS + "/" + taskInstanceId);
	}

	/**
	 * 既存タスクの照会
	 * 
	 * @throws URISyntaxException 
	 */
	protected LinkedHashMap referTasks() throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "SELECT ALL TASKS");
		LinkedHashMap resBody = execRestGetCall(URL_RU_TASKS);
		return resBody;
	}

	protected void referHistoricProcessInstances() throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "SELECT ALL HISTORIC PROCESS INSTANCES");
		execRestGetCall(URL_HI_PROCESS_INSTANCES + "?finished=true");
	}

	protected void deleteProcessInstance(int processInstanceId) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + " DELETE A SPESIFIC PROCESS INSTANCE");
		execRestDeleteCall(URL_RU_PROCESS_INSTANCES + "/" + processInstanceId);
	}

	protected void activateProcessInstance(int processInstanceId) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + " ACTIVATE A SPESIFIC PROCESS INSTANCE");

		JSONObject reqBody = new JSONObject();
		reqBody.put("action", "activate");
		
		execRestPutCall(URL_RU_PROCESS_INSTANCES + "/" + processInstanceId, reqBody);
	}

	protected void suspendProcessInstance(int processInstanceId) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + " SUSPEND A SPESIFIC PROCESS INSTANCE");

		JSONObject reqBody = new JSONObject();
		reqBody.put("action", "suspend");
		
		execRestPutCall(URL_RU_PROCESS_INSTANCES + "/" + processInstanceId, reqBody);
	}


	protected ArrayList<String> extractFromHashMapList(ArrayList<LinkedHashMap> activeProcessInstancesList, String keyStr) {
		ArrayList<String> valueList = new ArrayList<>();
		for (LinkedHashMap processInstance : activeProcessInstancesList) {
			valueList.add(processInstance.get(keyStr).toString());
		}
		return valueList;
	}

	protected void referProcessInstance(int processInstanceId) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + " SELECT A SPESIFIC PROCESS INSTANCE");
		execRestGetCall(URL_RU_PROCESS_INSTANCES + "/" + processInstanceId);
	}

	/**
	 * 既存プロセスインスタンスの生成
	 * 
	 * @throws URISyntaxException
	 */
	protected LinkedHashMap referProcessInstances() throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "SELECT ALL PROCESS INSTANCES");
		LinkedHashMap resBody = execRestGetCall(URL_RU_PROCESS_INSTANCES);
		return resBody;
	}

	/**
	 * 新規プロセスの開始
	 * 
	 * @param specificValue
	 * @throws URISyntaxException 
	 */
	protected void startProcess(String specificKey, String specificValue) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "START A PROCESS");

		JSONObject reqBody = new JSONObject();
		reqBody.put(specificKey, specificValue);
		reqBody.put("businessKey", "myBusinessKey");
		JSONObject nestedVariables = new JSONObject();
		nestedVariables.put("name", "myVar");
		nestedVariables.put("value", "This is a variable");
		JSONArray itemArray = new JSONArray();
		itemArray.put(nestedVariables);
		reqBody.put("variables", itemArray);
		
		execRestPostCall(URL_RU_PROCESS_INSTANCES, reqBody);
	}

	protected void referProcessDefinition(String prmKey, String prmValue) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "SELECT A SPECIFIC PROCESS_DEFINITION");
		execRestGetCall(URL_RE_PROCESS_DEFINITIONS + "?" + prmKey + "=" +prmValue);
	}

	/**
	 * 既存プロセス定義の照会
	 * 
	 * @throws URISyntaxException
	 */
	protected void referProcessDefinitions() throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "SELECT ALL FROM PROCESS_DEFINITION TABLE");
		execRestGetCall(URL_RE_PROCESS_DEFINITIONS);
	}

	protected void deleteUser(String userId) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "DELETE A SPECIFIC RECORD FROM USER TABLE");
		execRestDeleteCall(URL_ID_USERS + "/" + userId);		
	}

	protected void referUser(String userId) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "SELECT A SPECIFIC RECORD FROM USER TABLE");
		execRestGetCall(URL_ID_USERS + "/" + userId);		
	}

	protected void updateUser(String userId, Map<String, String> updateTargets) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "UPDATE A SPECIFIC RECORD IN USER TABLE");

		JSONObject reqBody = new JSONObject();
		for(Map.Entry<String, String> target : updateTargets.entrySet()) {
			reqBody.put(target.getKey(), target.getValue());
		}

		execRestPutCall(URL_ID_USERS + "/" + userId, reqBody);
	}

	/**
	 * 新規ユーザの登録
	 * 
	 * @param primaryChar
	 * @throws URISyntaxException
	 */
	protected void createUser(String primaryChar) throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "INSERT A RECORD INTO USER TABLE");

		JSONObject reqBody = new JSONObject();
		reqBody.put("id", primaryChar);
		reqBody.put("firstName", "Tijs");
		reqBody.put("lastName", "Barrez");
		reqBody.put("email", primaryChar + "@alfresco.org");
		reqBody.put("password", "pass123");

		execRestPostCall(URL_ID_USERS, reqBody);
	}

	/**
	 * 既存ユーザの照会
	 * 
	 * @throws URISyntaxException
	 */
	protected void referUsers() throws URISyntaxException {
		logMethodName();
		System.out.println(SIGN_METHOD_START + "SELECT ALL FROM USER TABLE");
		execRestGetCall(URL_ID_USERS);
	}

	protected void execRestPostCall(String requestPath, JSONObject reqBody) throws URISyntaxException {

		// POST用の要求エンティティ
		RequestEntity reqEntity = RequestEntity
				.post(new URI(URL_ROOT + requestPath))
				.header("Authorization", "Basic " + getStr4basicAuth())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(reqBody.toString());
		
		execRestCall(reqEntity, reqBody);
	}

	protected LinkedHashMap execRestGetCall(String requestPath) throws URISyntaxException {
		// GET用の要求エンティティ
		RequestEntity reqEntity = RequestEntity
				.get(new URI(URL_ROOT + requestPath))
				.header("Authorization", "Basic " + getStr4basicAuth())
				.accept(MediaType.APPLICATION_JSON)
				.build();
	
		return execRestCall(reqEntity, null);
	}

	protected void execRestPutCall(String requestPath, JSONObject reqBody) throws URISyntaxException {
		// PUT用の要求エンティティ
		RequestEntity reqEntity = RequestEntity
				.put(new URI(URL_ROOT + requestPath))
				.header("Authorization", "Basic " + getStr4basicAuth())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(reqBody.toString());
		
		execRestCall(reqEntity, reqBody);
		
	}

	protected void execRestDeleteCall(String requestPath) throws URISyntaxException {
		// DELETE用の要求エンティティ
		RequestEntity reqEntity = RequestEntity
				.delete(new URI(URL_ROOT + requestPath))
				.header("Authorization", "Basic " + getStr4basicAuth())
				.accept(MediaType.APPLICATION_JSON)
				.build();
	
		execRestCall(reqEntity, null);
	}

	/**
	 * リクエストエンティティに沿ってREST通信し、取得した結果をコンソール出力する。
	 * 
	 * @param reqEntity
	 * @param reqBodyInJson4log 
	 * @throws URISyntaxException
	 */
	private LinkedHashMap execRestCall(RequestEntity reqEntity, JSONObject reqBodyInJson4log) throws URISyntaxException {
		System.out.println("REQUEST:");
		HttpHeaders reqHeaders = reqEntity.getHeaders();
		System.out.println(reqHeaders.toString());
		if (reqBodyInJson4log != null) System.out.println(reqBodyInJson4log.toString(4));
		System.out.println();
		
		ResponseEntity<Object> resEntity = restTemplate.exchange(reqEntity, Object.class);
		System.out.println();
		
		System.out.println("RESPONSE:");
		HttpStatus statusCode = resEntity.getStatusCode();
		System.out.println(statusCode);
		HttpHeaders headers = resEntity.getHeaders();
		System.out.println(headers);
		Object body = resEntity.getBody();
		System.out.println(body);
		System.out.println();
		
		if (body instanceof LinkedHashMap) {
			System.out.print("formatting..\n");
			Gson gson = new Gson();
			String inJson = gson.toJson(body, LinkedHashMap.class);
			JSONObject bodyInJson = new JSONObject(inJson);
			System.out.println(bodyInJson.toString(4));
			System.out.println();
			
			return (LinkedHashMap) body;
		}
		return null;
	}

	private String createUniqueStrFromCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		Date date = new Date();
		return sdf.format(date);
	}

	/**
	 * Basic認証用の文字列の生成
	 * @return
	 */
	private String getStr4basicAuth() {
		byte[] bytes = (userId + ":" + userPw).getBytes();
		return new String(Base64.encodeBase64(bytes));
	}

	protected void logMethodName() {
		methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
	    System.out.println("\n" + SIGN_METHOD_START + methodName);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicRestClient client = new BasicRestClient();
		client.execute();

	}


}
