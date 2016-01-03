/* /////////////////////////////////////////////////////////////////////////
 * File:        test.scratch.inserter.lc_i.cpp
 *
 * Purpose:     Implementation file for the test.scratch.inserter.lc_i project.
 *
 * Created:     1st September 2012
 * Updated:     26th September 2015
 *
 * Status:      Wizard-generated
 *
 * License:     (Licensed under the Synesis Software Open License)
 *
 *              Copyright (c) 2012-2015, Synesis Software Pty Ltd.
 *              All rights reserved.
 *
 *              www:        http://www.synesis.com.au/software
 *
 * ////////////////////////////////////////////////////////////////////// */


/* /////////////////////////////////////////////////////////////////////////
 * Includes
 */

/* FastFormat header files */
#include <fastformat/inserters/lc_i.hpp>
#include <fastformat/sinks/ostream.hpp>
#include <fastformat/ff.hpp>

/* STLSoft header files */
#include <stlsoft/stlsoft.h>

#include <platformstl/platformstl.hpp>

/* Standard C++ header files */
#include <exception>
#include <iostream>
#if 0
#include <algorithm>
#include <iterator>
#include <list>
#include <map>
#include <numeric>
#include <set>
#include <string>
#include <vector>
#endif /* 0 */

/* Standard C header files */
#include <stdlib.h>

/* /////////////////////////////////////////////////////////////////////////
 * Globals
 */


/* /////////////////////////////////////////////////////////////////////////
 * Typedefs
 */

#if 0
typedef char                        char_t;
typedef std::basic_string<char_t>   string_t;
#endif /* 0 */

/* /////////////////////////////////////////////////////////////////////////
 * Forward declarations
 */

/* /////////////////////////////////////////////////////////////////////////
 * main()
 */

static int main_(int /* argc */, char** /*argv*/)
{
	::setlocale(LC_ALL, "german");

	ff::writeln(
		std::cout
	,	ff::lc_i(1234567890)
	);

    return EXIT_SUCCESS;
}

int main(int argc, char** argv)
{
#if 0
    { for(size_t i = 0; i < 0xffffffff; ++i){} }
#endif /* 0 */

    try
    {
#if defined(_DEBUG) || \
    defined(__SYNSOFT_DBS_DEBUG)
        std::cout << "test.scratch.inserter.lc_i: " << __STLSOFT_COMPILER_LABEL_STRING << std::endl;
#endif /* debug */

        return main_(argc, argv);
    }
    catch(std::bad_alloc&)
    {
        std::cerr << "out of memory" << std::endl;
    }
    catch(std::exception& x)
    {
        std::cerr << "Unhandled error: " << x.what() << std::endl;
    }
    catch(...)
    {
        std::cerr << "Unhandled unknown error" << std::endl;
    }

    return EXIT_FAILURE;
}

/* ///////////////////////////// end of file //////////////////////////// */
