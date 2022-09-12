package wikipedia.search.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class WikipediaContentExceptions {

	@ExceptionHandler({ RuntimeException.class })
	public ResponseEntity<String> handleRunTimeException(RuntimeException e) {
		return error(INTERNAL_SERVER_ERROR, e);
	}

	@ExceptionHandler({ InvalidPayloadException.class })
	public ResponseEntity<String> handleInvalidPayload(InvalidPayloadException e) {
		return error(NOT_FOUND, e);
	}
//
//	    @ExceptionHandler({DogsServiceException.class})
//	    public ResponseEntity<String> handleDogsServiceException(DogsServiceException e){
//	        return error(INTERNAL_SERVER_ERROR, e);
//	    }

	private ResponseEntity<String> error(HttpStatus status, Exception e) {
		return ResponseEntity.status(status).body(e.getMessage());
	}

}
