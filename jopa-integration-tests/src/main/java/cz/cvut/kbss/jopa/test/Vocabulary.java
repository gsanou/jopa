/**
 * Copyright (C) 2020 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jopa.test;

public class Vocabulary {

    public static final String CLASS_IRI_BASE = "http://krizik.felk.cvut.cz/ontologies/jopa/entities#";
    public static final String ATTRIBUTE_IRI_BASE = "http://krizik.felk.cvut.cz/ontologies/jopa/attributes#";

    public static final String C_OWL_CLASS_A = CLASS_IRI_BASE + "OWLClassA";
    public static final String C_OWL_CLASS_B = CLASS_IRI_BASE + "OWLClassB";
    public static final String C_OWL_CLASS_D = CLASS_IRI_BASE + "OWLClassD";
    public static final String C_OWL_CLASS_E = CLASS_IRI_BASE + "OWLClassE";
    public static final String C_OWL_CLASS_F = CLASS_IRI_BASE + "OWLClassF";
    public static final String C_OWL_CLASS_G = CLASS_IRI_BASE + "OWLClassG";
    public static final String C_OWL_CLASS_H = CLASS_IRI_BASE + "OWLClassH";
    public static final String C_OWL_CLASS_J = CLASS_IRI_BASE + "OWLClassJ";
    public static final String C_OWL_CLASS_L = CLASS_IRI_BASE + "OWLClassL";
    public static final String C_OWL_CLASS_M = CLASS_IRI_BASE + "OWLClassM";
    public static final String C_OWL_CLASS_S = CLASS_IRI_BASE + "OWLClassS";
    public static final String C_OWL_CLASS_T = CLASS_IRI_BASE + "OWLClassT";
    public static final String C_OWL_CLASS_U = CLASS_IRI_BASE + "OWLClassU";
    public static final String C_OWL_CLASS_Q = CLASS_IRI_BASE + "OWLClassQ";
    public static final String C_OWL_CLASS_S_PARENT = CLASS_IRI_BASE + "OWLClassSParent";
    public static final String C_OWL_CLASS_V = CLASS_IRI_BASE + "OWLClassV";
    public static final String C_OWL_CLASS_W = CLASS_IRI_BASE + "OWLClassW";
    public static final String C_OWL_CLASS_X = CLASS_IRI_BASE + "OWLClassX";

    public static final String p_m_booleanAttribute = ATTRIBUTE_IRI_BASE + "m-booleanAttribute";
    public static final String p_m_intAttribute = ATTRIBUTE_IRI_BASE + "m-intAttribute";
    public static final String p_m_longAttribute = ATTRIBUTE_IRI_BASE + "m-longAttribute";
    public static final String p_m_doubleAttribute = ATTRIBUTE_IRI_BASE + "m-doubleAttribute";
    public static final String p_m_dateAttribute = ATTRIBUTE_IRI_BASE + "m-dateAttribute";
    public static final String p_m_enumAttribute = ATTRIBUTE_IRI_BASE + "m-enumAttribute";
    public static final String p_m_IntegerSet = ATTRIBUTE_IRI_BASE + "m-pluralIntAttribute";
    public static final String p_m_lexicalForm = ATTRIBUTE_IRI_BASE + "m-lexicalForm";
    public static final String p_m_simpleLiteral = ATTRIBUTE_IRI_BASE + "m-simpleLiteral";

    public static final String P_N_STR_ANNOTATION_PROPERTY = ATTRIBUTE_IRI_BASE + "annotationProperty";
    public static final String P_N_STRING_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "N-stringAttribute";

    public static final String P_A_STRING_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "A-stringAttribute";
    public static final String P_B_STRING_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "B-stringAttribute";
    public static final String P_E_STRING_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "E-stringAttribute";
    public static final String P_F_HAS_SIMPLE_SET = ATTRIBUTE_IRI_BASE + "F-hasSimpleSet";

    public static final String P_T_INTEGER_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "T-integerAttribute";
    public static final String P_HAS_OWL_CLASS_A = ATTRIBUTE_IRI_BASE + "hasOwlClassA";

    public static final String P_Q_PARENT_STRING_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "QParent-stringAttribute";
    public static final String P_Q_STRING_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "Q-stringAttribute";

    public static final String P_HAS_OWL_CLASS_S = ATTRIBUTE_IRI_BASE + "hasOwlClassS";

    public static final String V_HAS_THING = ATTRIBUTE_IRI_BASE + "hasThing";

    public static final String DC_DESCRIPTION = "http://purl.org/dc/terms/description";
    public static final String DC_SOURCE = "http://purl.org/dc/terms/source";

    public static final String P_HAS_SIMPLE_LIST =
            "http://krizik.felk.cvut.cz/ontologies/jopa/attributes#C-hasSimpleSequence";
    public static final String P_HAS_REFERENCED_LIST =
            "http://krizik.felk.cvut.cz/ontologies/jopa/attributes#C-hasReferencedSequence";

    public static final String P_X_LOCAL_DATE_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "xLocalDate";
    public static final String P_X_LOCAL_DATETIME_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "xLocalDateTime";
    public static final String P_X_INSTANT_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "xInstant";
    public static final String P_X_OBJECT_ATTRIBUTE = ATTRIBUTE_IRI_BASE + "xObject";

    public static final String P_HAS_H = ATTRIBUTE_IRI_BASE + "hasH";

    private Vocabulary() {
        throw new AssertionError();
    }
}
