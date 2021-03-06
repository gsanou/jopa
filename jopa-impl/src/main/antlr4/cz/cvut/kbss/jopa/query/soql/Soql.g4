grammar Soql;


querySentence : selectStatement whereClauseWrapper? groupByClause? orderByClause? ;



selectStatement: typeDef params FROM tables ;

typeDef: SELECT ;

params: paramComma* distinctParam ;

paramComma: distinctParam COMMA ;

distinctParam: distinct? selectedParam ;

selectedParam: param | count;

count: COUNT LEFTPAREN param RIGHTPAREN ;

param: objWithAttr | objWithOutAttr ;

objWithAttr: object DOT attribute;

objWithOutAttr: object ;

distinct: DISTINCT ;

object: TEXT ;

attribute: TEXT ;

joinedParams: object DOT attribute (DOT attribute)+ ;



tables: tableWithName ;

table: TEXT ;

tableName: TEXT ;

tableWithName: table tableName ;



logOp: AND | OR ;



whereClauseWrapper: WHERE whereClauses ;

whereClauses: whereClauseOps whereClauseOps* ;

whereClauseOps: logOp? NOT? whereClause ;

whereClause: whereClauseParam QUERYOPERATOR whereClauseValue;

whereClauseValue: (QMARK TEXT QMARK) | COLONTEXT ;

whereClauseParam: param | joinedParams ;



orderByClause: ORDERBY orderByFullFormComma orderByFullFormComma* ;

orderByFullFormComma: orderByFullForm COMMA? ;

orderByFullForm: orderByParam ORDERING? ;

orderByParam: object DOT attribute (DOT attribute)* ;



groupByClause: GROUPBY groupByParamComma groupByParamComma* ;

groupByParamComma: groupByParam COMMA? ;

groupByParam: object DOT attribute (DOT attribute)* ;



SELECT: 'SELECT' ;

WHERE: 'WHERE' ;

NOT: 'NOT' ;

FROM: 'FROM' ;

JOIN: 'JOIN' ;

AND: 'AND' ;

OR: 'OR' ;

ORDERBY: 'ORDER BY' ;

ORDERING: ASC | DESC ;

GROUPBY: 'GROUP BY' ;

ASC: 'ASC' ;

DESC: 'DESC' ;

DISTINCT: 'DISTINCT' ;

COUNT: 'COUNT' ;

QUERYOPERATOR: '>' | '<' | '>=' | '<=' | '=' | 'LIKE';

DOT: '.' ;

COMMA: ',' ;

QMARK: '"' ;

COLON: ':' ;

RIGHTPAREN: ')' ;

LEFTPAREN: '(' ;

TEXT: (LOWERCASE | UPPERCASE | DIGIT)+ ;

COLONTEXT: COLON TEXT ;

UPPERCASE: ('A'..'Z');

LOWERCASE: ('a'..'z');

DIGIT: ('0'..'9');

NUMBER: DIGIT+ ;

VALUE: NUMBER ;

WHITESPACE: (' ')+ -> skip;
