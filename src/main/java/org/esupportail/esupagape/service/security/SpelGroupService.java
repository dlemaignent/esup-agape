package org.esupportail.esupagape.service.security;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpelGroupService implements GroupService {

	private Map<String, String> groups4eppnSpel = new HashMap<>();

	public void setGroups4eppnSpel(Map<String, String> groups4eppnSpel) {
		this.groups4eppnSpel = groups4eppnSpel;
	}

	@Override
	public List<String> getGroups(String eppn) {

		List<String> groups = new ArrayList<>();

		for(String groupName: groups4eppnSpel.keySet()) {
			String expression = groups4eppnSpel.get(groupName);
			ExpressionParser parser = new SpelExpressionParser();
			Expression exp = parser.parseExpression(expression);
			EvaluationContext context = new StandardEvaluationContext();
			context.setVariable("eppn", eppn);
			
			Boolean value = (Boolean) exp.getValue(context);
			if(value) {
				groups.add(groupName);
			}
		}		
		return groups.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
		
	}

}