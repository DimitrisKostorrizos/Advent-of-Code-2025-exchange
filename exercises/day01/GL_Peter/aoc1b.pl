
my $totalscore = 0;
my $pos = 50;
my $oldval = 0;
my $newval = 9;

while (<>) {
	chop();	
	my ($dir,$val) = ($_ =~ m/(\w)(\d+)/);
	if ($dir eq "L") {
		if ($pos==0) {
			$totalscore += int($val/100);
		} else {
			$totalscore += int((100-$pos+$val)/100);
		}
		$pos = 100-((100-$pos+$val)%100) ;
		if ($pos==100) {
			$pos = 0;
		}
	} elsif ($dir eq "R") {
		$totalscore += int(($pos+$val)/100);
		$pos = ($pos+$val)%100;
	}

print "$dir$val $pos $totalscore\n";
}

print "\n== $totalscore\n";
