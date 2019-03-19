TA-Lib-Java-Examples
====================

<pre>
Couple usage examples of the TA-LIb Java API.
Originally found here: https://github.com/ishanthilina/TA-Lib-Java-Examples.git

Then modified to implement an example of EMA and SMA calculation described in this article:
ChartSchool » Technical Indicators and Overlays » Moving Averages - Simple and Exponential
https://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_averages

Expected output (table copied from the article linked above)

	Date		Price	10-day SMA	Smoothing Constant 2/(10 + 1)	10-day EMA
1	24-Mar-10	22.27			
2	25-Mar-10	22.19			
3	26-Mar-10	22.08			
4	29-Mar-10	22.17			
5	30-Mar-10	22.18			
6	31-Mar-10	22.13			
7	1-Apr-10	22.23			
8	5-Apr-10	22.43			
9	6-Apr-10	22.24			
10	7-Apr-10	22.29	22.22		22.22
11	8-Apr-10	22.15	22.21	0.1818	22.21
12	9-Apr-10	22.39	22.23	0.1818	22.24
13	12-Apr-10	22.38	22.26	0.1818	22.27
14	13-Apr-10	22.61	22.31	0.1818	22.33
15	14-Apr-10	23.36	22.42	0.1818	22.52
16	15-Apr-10	24.05	22.61	0.1818	22.80
17	16-Apr-10	23.75	22.77	0.1818	22.97
18	19-Apr-10	23.83	22.91	0.1818	23.13
19	20-Apr-10	23.95	23.08	0.1818	23.28
20	21-Apr-10	23.63	23.21	0.1818	23.34
21	22-Apr-10	23.82	23.38	0.1818	23.43
22	23-Apr-10	23.87	23.53	0.1818	23.51
23	26-Apr-10	23.65	23.65	0.1818	23.54
24	27-Apr-10	23.19	23.71	0.1818	23.47
25	28-Apr-10	23.10	23.69	0.1818	23.40
26	29-Apr-10	23.33	23.61	0.1818	23.39
27	30-Apr-10	22.68	23.51	0.1818	23.26
28	3-May-10	23.10	23.43	0.1818	23.23
29	4-May-10	22.40	23.28	0.1818	23.08
30	5-May-10	22.17	23.13	0.1818	22.92

output of ./run_SimpleMovingAverageExample:

To compare ta-lib calculated SMA and EMA vs. expected output see also this google spreadsheet:
https://docs.google.com/spreadsheets/d/1JDLc1X1zPSWqKnut10nltimgoGzz9KMG5q6XpdRz6hw/edit?usp=sharing

</pre>
