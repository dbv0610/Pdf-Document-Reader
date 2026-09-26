/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.ss.usermodel.DataValidation
import com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint
import com.wxiwei.office.fc.ss.usermodel.DataValidationHelper
import com.wxiwei.office.fc.ss.util.CellRangeAddressList


/**
 * @author [Radhakrishnan J](rjankiraman@emptoris.com)
 */
class HSSFDataValidationHelper(@field:Suppress("unused") private val sheet: HSSFSheet?) :
    DataValidationHelper {
    /*
          * (non-Javadoc)
          * 
          * @see
          * org.apache.poi.ss.usermodel.DataValidationHelper#createDateConstraint
          * (int, java.lang.String, java.lang.String, java.lang.String)
          */
    override fun createDateConstraint(
        operatorType: Int,
        formula1: String?,
        formula2: String?,
        dateFormat: String?
    ): DataValidationConstraint {
        return DVConstraint.Companion.createDateConstraint(
            operatorType,
            formula1 ?: "",
            formula2,
            dateFormat
        )
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createExplicitListConstraint
	 * (java.lang.String[])
	 */
    override fun createExplicitListConstraint(listOfValues: Array<String?>?): DataValidationConstraint {
        return DVConstraint.Companion.createExplicitListConstraint(listOfValues)
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createFormulaListConstraint
	 * (java.lang.String)
	 */
    override fun createFormulaListConstraint(listFormula: String?): DataValidationConstraint {
        return DVConstraint.Companion.createFormulaListConstraint(listFormula)
    }


    override fun createNumericConstraint(
        validationType: Int,
        operatorType: Int,
        formula1: String?,
        formula2: String?
    ): DataValidationConstraint {
        return DVConstraint.Companion.createNumericConstraint(
            validationType,
            operatorType,
            formula1,
            formula2
        )
    }

    override fun createIntegerConstraint(
        operatorType: Int,
        formula1: String?,
        formula2: String?
    ): DataValidationConstraint {
        return DVConstraint.Companion.createNumericConstraint(
            DataValidationConstraint.ValidationType.INTEGER,
            operatorType,
            formula1,
            formula2
        )
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createNumericConstraint
	 * (int, java.lang.String, java.lang.String)
	 */
    override fun createDecimalConstraint(
        operatorType: Int,
        formula1: String?,
        formula2: String?
    ): DataValidationConstraint {
        return DVConstraint.Companion.createNumericConstraint(
            DataValidationConstraint.ValidationType.DECIMAL,
            operatorType,
            formula1,
            formula2
        )
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createTextLengthConstraint
	 * (int, java.lang.String, java.lang.String)
	 */
    override fun createTextLengthConstraint(
        operatorType: Int,
        formula1: String?,
        formula2: String?
    ): DataValidationConstraint {
        return DVConstraint.Companion.createNumericConstraint(
            DataValidationConstraint.ValidationType.TEXT_LENGTH,
            operatorType,
            formula1,
            formula2
        )
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createTimeConstraint
	 * (int, java.lang.String, java.lang.String, java.lang.String)
	 */
    override fun createTimeConstraint(
        operatorType: Int,
        formula1: String?,
        formula2: String?
    ): DataValidationConstraint {
        return DVConstraint.createTimeConstraint(operatorType, formula1 ?: "", formula2)
    }


    override fun createCustomConstraint(formula: String?): DataValidationConstraint {
        return DVConstraint.createCustomFormulaConstraint(formula ?: "")
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createValidation(org
	 * .apache.poi.ss.usermodel.DataValidationConstraint,
	 * org.apache.poi.ss.util.CellRangeAddressList)
	 */
    override fun createValidation(
        constraint: DataValidationConstraint?,
        cellRangeAddressList: CellRangeAddressList?
    ): DataValidation {
        return HSSFDataValidation(cellRangeAddressList!!, constraint)
    }
}
