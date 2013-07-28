package sybyla.ey;

import java.util.List;

public class ContractAnalyzer {
	
	private String part1;
	private String part2;
	private String value;
	private String contractDate;
	private String beginDate;
	private String endDate;
	
	public String analyze(String text) {
					
			List<String> parts =  PartsModel.evaluate(text);
			part1 =  parts.get(0);
			if (part1==null){
				part1="";
			}
			part2 =  parts.get(1);
			if (part2==null){
				part2="";
			}
			
			value = CurrencyModel.getMaxValue(text);
			if(value==null){
				value="";
			}

			List<Tag> dates = DateModel.findDates(text);
			String t =  PartsModel.normalize(text);
			contractDate = DateModel.findContractDate(dates, t);
			if (contractDate==null){
				contractDate="";
			}
			
			String[] beginEndDates=DateModel.findBeginEndDates(dates, text);
			beginDate=beginEndDates[0];
			if (beginDate == null){
				beginDate="";
			}
			endDate=beginEndDates[1];
			if(endDate == null){
				endDate="";
			}
			
			StringBuffer sb = new StringBuffer();
			sb.append("{\"part1\":").append("\"").append(part1).append("\",");
			sb.append("\"part2\":").append("\"").append(part2).append("\",");
			sb.append("\"value\":").append("\"").append(value).append("\",");
			sb.append("\"contractDate\":").append("\"").append(contractDate).append("\",");
			sb.append("\"beginDate\":").append("\"").append(beginDate).append("\",");
			sb.append("\"endDate\":").append("\"").append(endDate).append("\"}");
			
			return sb.toString();
	}

	public String getPart1() {
		return part1;
	}

	public String getPart2() {
		return part2;
	}
	
	public String getValue() {
		return value;
	}

	public String getContractDate() {
		return contractDate;
	}

	public String getBeginDate() {
		return beginDate;
	}

	public String getEndDate() {
		return endDate;
	}
}
