import sys
import csv
y = lambda x: reduce(lambda x_,y_ : x_+y_, x )
path = sys.argv[1] if len(sys.argv) > 1 else "test.csv"
with open(path, "rb") as f:
	reader = csv.DictReader(f, delimiter=",")
	with open("out.csv","wb") as out:
		field_names = ["Time", "Min", "Max", "Avg", "Diff"]
		writer = csv.DictWriter(out,delimiter=",",fieldnames = field_names)
		writer.writeheader()
		for row in reader:
			max = float(row["Max"].replace(",","."))
			min = float(row["Min"].replace(",","."))
			Time = row["Time"]
			row["Time"] = Time[:4] + "-" + Time[4:6] + "-" + Time[6:8] + " " + Time[9:11] + ":" + Time[11:13]
			avg ="{0:.5f}".format((min + max)/2).replace(".",",")
			diff = "{0:.5f}".format(abs(max - min)).replace(".",",")
			row["Avg"] = avg
			row["Diff"] = diff
			writer.writerow(row)