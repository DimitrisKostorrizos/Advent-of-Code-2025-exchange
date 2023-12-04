
my @digit = ("", "one", "two","three","four","five","six","seven","eight","nine");
my $totalscore = 0;
while (<>) {
	chop();
	my $left = '';
	my $right = '';
	my $inp = $_;
	while (!length($left)) {
        ( $left ) = ($inp =~ m/^(\d)/);
		length($left) or break;
		for (my $d=1 ; $d<=$#digit;$d++) {
			if ($inp =~ m/^$digit[$d]/) {
				$left = $d;
				break;
			}
		}
		$inp = substr($inp,1);
	}
	my $inp = $_;
	while (!length($right)) {
        ( $right ) = ($inp =~ m/(\d)$/);
		length($right) or break;
		for (my $d=1 ; $d<=$#digit;$d++) {
			if ($inp =~ m/$digit[$d]$/) {
				$right = $d;
				break;
			}
		}
		$inp = substr($inp,0,length($inp)-1);
	}
	$totalscore += $left.$right;
}

print "\n== $totalscore\n";
