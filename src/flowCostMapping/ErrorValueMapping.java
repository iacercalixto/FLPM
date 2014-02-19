/**
 * 
 */
package flowCostMapping;

/**
 * @author iacer
 * A class containing information on the relative error measurements
 * to use with link counts, OD counts, O-only and D-only counts.
 */
public class ErrorValueMapping {
	float[] errorAInferior;
	float[] errorASuperior;
	float[] errorAValue;
	
	float[] errorBInferior;
	float[] errorBSuperior;
	float[] errorBValue;
	
	float[] errorDInferior;
	float[] errorDSuperior;
	float[] errorDValue;
	
	float[] errorEInferior;
	float[] errorESuperior;
	float[] errorEValue;
	
	public enum CountType
	{
		LinkCount,
		ODMeasurement,
		OriginOnlyMeasurement,
		DestinationOnlyMeasurement
	}
	
	public ErrorValueMapping() {
		// Initialize erorr value mappings to all existing counts
		
		// Link counts errors ranges (a) are:
		// 0-50			90%
		// 51-100		80%
		// 101-200		70%
		// 201-300		60%
		// 301-400		50%
		// 401-500		40%
		// 501-750		30%
		// 751-1000		20%
		// 1000-above	10%
		errorAInferior = new float[9];
		errorASuperior = new float[9];
		errorAValue = new float[9];
		errorAInferior[0] = 0f;
		errorASuperior[0] = 50f;
		errorAValue[0] = 0.9f;
		errorAInferior[1] = 51f;
		errorASuperior[1] = 100f;
		errorAValue[1] = 0.8f;
		errorAInferior[2] = 101f;
		errorASuperior[2] = 200f;
		errorAValue[2] = 0.7f;
		errorAInferior[3] = 201f;
		errorASuperior[3] = 300f;
		errorAValue[3] = 0.6f;
		errorAInferior[4] = 301f;
		errorASuperior[4] = 400f;
		errorAValue[4] = 0.5f;
		errorAInferior[5] = 401f;
		errorASuperior[5] = 500f;
		errorAValue[5] = 0.4f;
		errorAInferior[6] = 501f;
		errorASuperior[6] = 750f;
		errorAValue[6] = 0.3f;
		errorAInferior[7] = 751f;
		errorASuperior[7] = 1000f;
		errorAValue[7] = 0.2f;
		errorAInferior[8] = 1000f;
		errorASuperior[8] = Float.POSITIVE_INFINITY;
		errorAValue[8] = 0.1f;
		
		// OD measurement errors ranges (b) are:
		// 0-50			90%
		// 51-100		90%
		// 101-200		90%
		// 201-300		60%
		// 301-400		50%
		// 401-500		40%
		// 501-750		30%
		// 751-1000		20%
		// 1000-above	10%
		errorBInferior = new float[9];
		errorBSuperior = new float[9];
		errorBValue = new float[9];
		errorBInferior[0] = 0f;
		errorBSuperior[0] = 50f;
		errorBValue[0] = 0.9f;
		errorBInferior[1] = 51f;
		errorBSuperior[1] = 100f;
		errorBValue[1] = 0.9f;
		errorBInferior[2] = 101f;
		errorBSuperior[2] = 200f;
		errorBValue[2] = 0.9f;
		errorBInferior[3] = 201f;
		errorBSuperior[3] = 300f;
		errorBValue[3] = 0.6f;
		errorBInferior[4] = 301f;
		errorBSuperior[4] = 400f;
		errorBValue[4] = 0.5f;
		errorBInferior[5] = 401f;
		errorBSuperior[5] = 500f;
		errorBValue[5] = 0.4f;
		errorBInferior[6] = 501f;
		errorBSuperior[6] = 750f;
		errorBValue[6] = 0.3f;
		errorBInferior[7] = 751f;
		errorBSuperior[7] = 1000f;
		errorBValue[7] = 0.2f;
		errorBInferior[8] = 1000f;
		errorBSuperior[8] = Float.POSITIVE_INFINITY;
		errorBValue[8] = 0.1f;
		
		// Origin-only measurement errors ranges (d) are:
		// 0-50			90%
		// 51-100		80%
		// 101-200		70%
		// 201-300		60%
		// 301-400		50%
		// 401-500		40%
		// 501-750		30%
		// 751-1000		20%
		// 1000-above	10%
		errorDInferior = new float[9];
		errorDSuperior = new float[9];
		errorDValue = new float[9];
		errorDInferior[0] = 0f;
		errorDSuperior[0] = 50f;
		errorDValue[0] = 0.9f;
		errorDInferior[1] = 51f;
		errorDSuperior[1] = 100f;
		errorDValue[1] = 0.8f;
		errorDInferior[2] = 101f;
		errorDSuperior[2] = 200f;
		errorDValue[2] = 0.7f;
		errorDInferior[3] = 201f;
		errorDSuperior[3] = 300f;
		errorDValue[3] = 0.6f;
		errorDInferior[4] = 301f;
		errorDSuperior[4] = 400f;
		errorDValue[4] = 0.5f;
		errorDInferior[5] = 401f;
		errorDSuperior[5] = 500f;
		errorDValue[5] = 0.4f;
		errorDInferior[6] = 501f;
		errorDSuperior[6] = 750f;
		errorDValue[6] = 0.3f;
		errorDInferior[7] = 751f;
		errorDSuperior[7] = 1000f;
		errorDValue[7] = 0.2f;
		errorDInferior[8] = 1000f;
		errorDSuperior[8] = Float.POSITIVE_INFINITY;
		errorDValue[8] = 0.1f;
		
		// Destination-only measurement errors ranges (e) are:
		// 0-50			100%
		// 51-100		100%
		// 101-200		100%
		// 201-300		100%
		// 301-400		100%
		// 401-500		100%
		// 501-750		100%
		// 751-1000		100%
		// 1000-above	100%
		errorEInferior = new float[9];
		errorESuperior = new float[9];
		errorEValue = new float[9];
		errorEInferior[0] = 0f;
		errorESuperior[0] = 50f;
		errorEValue[0] = 1.0f;
		errorEInferior[1] = 51f;
		errorESuperior[1] = 100f;
		errorEValue[1] = 1.0f;
		errorEInferior[2] = 101f;
		errorESuperior[2] = 200f;
		errorEValue[2] = 1.0f;
		errorEInferior[3] = 201f;
		errorESuperior[3] = 300f;
		errorEValue[3] = 1.0f;
		errorEInferior[4] = 301f;
		errorESuperior[4] = 400f;
		errorEValue[4] = 1.0f;
		errorEInferior[5] = 401f;
		errorESuperior[5] = 500f;
		errorEValue[5] = 1f;
		errorEInferior[6] = 501f;
		errorESuperior[6] = 750f;
		errorEValue[6] = 1f;
		errorEInferior[7] = 751f;
		errorESuperior[7] = 1000f;
		errorEValue[7] = 1f;
		errorEInferior[8] = 1000f;
		errorESuperior[8] = Float.POSITIVE_INFINITY;
		errorEValue[8] = 1f;
	}
	
	public double getRelativeError(CountType countType, double countValue)
	throws Exception {
		if (countValue<0)
			throw new Exception("The count provided can't be a negative number.");
		
		double returnError = 0;
		
		if ( countType.equals(CountType.LinkCount) )
		{
			for(int i=0; i<errorAInferior.length; i++)
			{
				// If the value provided is within the interval,
				// set its error value
				if (countValue >= errorAInferior[i] && countValue <= errorASuperior[i])
					returnError = errorAValue[i] * countValue;
			}
		}
		
		if ( countType.equals(CountType.ODMeasurement) )
		{
			for(int i=0; i<errorBInferior.length; i++)
			{
				// If the value provided is within the interval,
				// set its error value
				if (countValue >= errorBInferior[i] && countValue <= errorBSuperior[i])
					returnError = errorBValue[i] * countValue;
			}
		}
		
		if ( countType.equals(CountType.OriginOnlyMeasurement) )
		{
			for(int i=0; i<errorDInferior.length; i++)
			{
				// If the value provided is within the interval,
				// set its error value
				if (countValue >= errorDInferior[i] && countValue <= errorDSuperior[i])
					returnError = errorDValue[i] * countValue;
			}
		}
		
		if ( countType.equals(CountType.DestinationOnlyMeasurement) )
		{
			for(int i=0; i<errorEInferior.length; i++)
			{
				// If the value provided is within the interval,
				// set its error value
				if (countValue >= errorEInferior[i] && countValue <= errorESuperior[i])
					returnError = errorEValue[i] * countValue;
			}
		}
		
		if (returnError<0)
			System.out.println("returnError: " + returnError);
		
		return returnError;
	}
}
