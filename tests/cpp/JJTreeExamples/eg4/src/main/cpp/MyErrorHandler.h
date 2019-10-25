/*
 * MyErrorHandler.h
 *
 *  Created on: 12 avr. 2014
 *      Author: FrancisANDRE
 */

#ifndef MYERRORHANDLER_H_
#define MYERRORHANDLER_H_
#include "ErrorHandler.h"

namespace EG4 {

class MyErrorHandler : public ErrorHandler {
public:
	MyErrorHandler();
	virtual ~MyErrorHandler();
};

} /* namespace EG4 */

#endif /* MYERRORHANDLER_H_ */
