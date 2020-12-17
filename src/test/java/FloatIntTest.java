import com.niton.util.FloatInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FloatIntTest {
	@Test
	public void parseNumber(){
		Assertions.assertEquals("123.456",FloatInt.formatAsFloat(123456, 3) );
	}
}
