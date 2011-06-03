package ch.epfl.bbcf.gdv.control.http.command;

import java.io.PrintWriter;

import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.access.database.pojo.Job;
import ch.epfl.bbcf.gdv.access.database.pojo.Status;
import ch.epfl.bbcf.gdv.control.http.RequestParameters;
import ch.epfl.bbcf.gdv.control.model.JobControl;

public class JobAccess extends Command{




	public JobAccess(RequestParameters params, PrintWriter out) {
		super(params, out);
	}

	@Override
	public void doRequest() {
		log.debug("job access : doRequest");
		if(null==params.getAction()){
			throw new AbortWithHttpStatusException(400,true);
		}
		switch(params.getAction()){
		case gfeatminer:
			break;
		case new_selection:
			checkParams(params.getSelections());
			checkParams(params.getNrAssemblyId(),params.getProjectId());
			int jobId;
			try {
				jobId = JobControl.newSelection(params.getSelections(), params.getProjectId(), params.getNrAssemblyId(),params.getData());
				Job job = JobControl.getJob(jobId);
				if(null!=job){
					out.write(outputJob(job));
				} else {
					throw new AbortWithHttpStatusException(500,true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new AbortWithHttpStatusException(500,true);
			} finally {
				out.close();
			}
			break;
		case status :
			checkParams(params.getJobId());
			Job job = JobControl.getJob(params.getJobId());
			out.write(outputJob(job));
			out.close();
			break;
		default:throw new AbortWithHttpStatusException(400,true);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * write the output of a job,
	 * depending on his status
	 * @param job the job
	 * @return a String in JSON format that will be parser
	 * by the javascript
	 */
	private String outputJob(Job job){
		String output = null;
		int status = job.getStatus();
		if(status==Status.RUNNING){
			output = "{job_id:"+job.getId()+",status:\"running\"}";
		} else if(status==Status.ERROR){
			output = "{job_id:"+job.getId()+",status:\"error\",data:\""+job.getData()+"\"}";
		} else if(status==Status.SUCCES){
			output = "{job_id:"+job.getId()+",status:\"success\",output:\""+job.getOutput()+"\",data:\""+job.getData()+"\"}";
		}
		return output;
	}
	
	
	
	
	
	
	
}
